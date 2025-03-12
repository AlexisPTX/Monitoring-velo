#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include "MAX30105.h"
#include <OneWire.h>
#include <DallasTemperature.h>

// Définition de la broche pour la détection de la tension
#define COMP_PIN A1  // Broche à comparer
#define VCC 5.0      // Tension d'entrée de la carte
#define THRESHOLD 1.0 // Seuil de détection de 1V

// Initialisation des capteurs
MAX30105 particleSensor;
LiquidCrystal_I2C lcd(0x20, 20, 4);
OneWire oneWire(4);
DallasTemperature sensors(&oneWire);

const byte RATE_SIZE = 4;
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;
float beatsPerMinute = 0;
int beatAvg = 50;
long lastIRValue = 0;

int countChanges = 0;  // Compteur de changements
bool previousState = false; // État précédent (A1 < 5V ou A1 >= 5V)

// Délais pour les différentes tâches
unsigned long lastSensorReadTime = 0;  // Temps de la dernière lecture des capteurs
unsigned long lastSendTime = 0;        // Temps du dernier envoi des données
unsigned long lastChangeCountTime = 0; // Temps de la dernière mise à jour du compteur
unsigned long lastSpeedCalcTime = 0;   // Temps de la dernière mise à jour de la vitesse
const unsigned long sensorReadInterval = 5000;  // Intervalle de lecture des capteurs (5 secondes)
const unsigned long sendInterval = 30000;       // Intervalle d'envoi des données (30 secondes)
const unsigned long changeCountInterval = 100;  // Intervalle de mise à jour du compteur de changements (100 ms)

// Variable pour calculer la distance parcourue
float totalDistanceMeters = 0.0;  // Distance totale parcourue en mètres
float speedKmh = 0.0;  // Vitesse en km/h

void setup() {
    Serial1.begin(9600); // Communication TX1/RX1 avec la SODAQ
    Serial.begin(57600); // Debug via Moniteur Série
    lcd.init();
    lcd.backlight();
    sensors.begin();

    // Initialisation du capteur MAX30102
    if (!particleSensor.begin()) {
        Serial.println("MAX30102 non détecté");
        lcd.setCursor(0, 0);
        lcd.print("Capteur BPM absent");
        while (1);
    }
    particleSensor.setup();
    particleSensor.setPulseAmplitudeRed(0xFF);
    particleSensor.setPulseAmplitudeGreen(0);

    pinMode(COMP_PIN, INPUT);
}

void loop() {
    unsigned long currentMillis = millis();

    // Mise à jour du compteur à chaque itération (sans délai)
    if (currentMillis - lastChangeCountTime >= changeCountInterval) {
        lastChangeCountTime = currentMillis;

        // Lecture de la tension sur A1 (conversion en volts)
        float voltageA1 = analogRead(COMP_PIN) * (VCC / 1023.0);

        // Vérifier si la tension est passée de 5V à 0V
        bool currentState = (voltageA1 > 4.5); // True si A1 >= 4.5V, False si A1 < 0.5V

        // Incrémenter le compteur uniquement si la tension passe de 5V à 0V
        if (previousState && !currentState) { // Si l'état précédent était "5V" (true) et l'état actuel est "0V" (false)
            if (countChanges < 255) {  // Limiter à 255 (1 octet)
                countChanges++; // Incrémenter le compteur
                totalDistanceMeters += 6.0; // Ajouter 4 mètres pour chaque changement
            }
            Serial.print("Changement détecté ! Nombre total : ");
            Serial.println(countChanges);
        }

        previousState = currentState; // Mettre à jour l'état précédent
    }

    // Mise à jour des capteurs à intervalles réguliers (5 secondes)
    if (currentMillis - lastSensorReadTime >= sensorReadInterval) {
        lastSensorReadTime = currentMillis;

        // Lecture du capteur MAX30102 (BPM)
        long irValue = particleSensor.getIR();
        if (irValue > lastIRValue + 100 && currentMillis - lastBeat > 300) {
            long delta = currentMillis - lastBeat;
            lastBeat = currentMillis;
            beatsPerMinute = 60.0 / (delta / 10000.0);
            Serial.print(beatsPerMinute);
            if (beatsPerMinute > 20 && beatsPerMinute < 255) {
                rates[rateSpot++] = (byte)beatsPerMinute;
                rateSpot %= RATE_SIZE;
                beatAvg = 0;
                for (byte x = 0; x < RATE_SIZE; x++) {
                    beatAvg += rates[x];
                }
                beatAvg /= RATE_SIZE;
            }
        }
        lastIRValue = irValue;

        // Lecture du capteur de température
        sensors.requestTemperatures();
        float tempC = sensors.getTempCByIndex(0);

        // Calcul de la vitesse (km/h) à partir de la distance parcourue
        if (currentMillis - lastSpeedCalcTime >= 1000) {  // Calcul de la vitesse chaque seconde
            lastSpeedCalcTime = currentMillis;

            // Calcul de la vitesse en km/h (distance en m / temps écoulé en heures)
            speedKmh = totalDistanceMeters*3.6/5;  // Distance en kilomètres / 1 heure (parce que chaque seconde)
            
            // Réinitialiser la distance parcourue chaque seconde
            totalDistanceMeters = 0.0;  // Reset distance every second

            Serial.print("Vitesse en km/h: ");
            Serial.println(speedKmh);
        }

        // Affichage LCD
        lcd.setCursor(0, 0);
        lcd.print("Temp: ");
        lcd.print("BPM: ");
        lcd.print("SPD: ");
        lcd.setCursor(0, 1);
        lcd.print(tempC);
        lcd.print("C ");
        lcd.print(beatAvg);
        lcd.print("  ");
        lcd.print(speedKmh);  // Affichage de la vitesse sur l'écran LCD
    }

    // Envoi des données à intervalles réguliers (30 secondes)
    if (currentMillis - lastSendTime >= sendInterval) {
        lastSendTime = currentMillis;

        // Préparer les données à envoyer
        uint8_t data[3];  // 1 pour le BPM (byte), 1 pour la température (byte), 1 pour la vitesse (byte), 1 pour la tension (byte)

        // Remplir le tableau avec les valeurs à envoyer :
        data[0] = (uint8_t)beatAvg;  // BPM
        data[1] = (uint8_t)round(sensors.getTempCByIndex(0));  // Température arrondie (en entier)
        data[2] = (uint8_t)speedKmh;  // Vitesse en km/h (1 octet, à arrondir)


        // Envoi du tableau de données (BPM, Temp, Speed, Voltage sous forme d'octets)
        Serial1.write(data, sizeof(data));  // Envoi de 4 octets

        Serial.println("Données envoyées via LoRa");
    }

    // Ajouter un délai de 100 ms (0.1 seconde) entre chaque itération du loop()
    delay(50);
}
