#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include "MAX30105.h"
#include <OneWire.h>
#include <DallasTemperature.h>

// Initialisation du capteur de fréquence cardiaque
MAX30105 particleSensor;
LiquidCrystal_I2C lcd(0x20, 20, 4); // Adresse I2C de l'écran LCD (ajustez si nécessaire)

const byte RATE_SIZE = 4;  // Nombre d'échantillons pour calculer la moyenne
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;

float beatsPerMinute = 0;
int beatAvg = 0;

const long threshold = 2000;   // Seuil pour détecter un battement (à ajuster)
const long minInterval = 300;  // Intervalle minimum entre deux battements (ms)
long lastIRValue = 0;          // Dernière valeur IR pour comparaison

// Initialisation du capteur de température
#define ONE_WIRE_BUS 4           // Broche pour le capteur de température
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

void setup() {
  Serial.begin(115200);

  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Initialisation...");

  // Initialisation du capteur MAX30102
  if (!particleSensor.begin()) {
    Serial.println("MAX30102 non detecte");
    lcd.setCursor(0, 1);
    lcd.print("Capteur manquant!");
    while (1); // Bloque si le capteur n'est pas trouvé
  }

  lcd.setCursor(0, 1);
  lcd.print("Capteur OK");

  delay(2000);
  lcd.clear();

  particleSensor.setup();
  particleSensor.setPulseAmplitudeRed(0x7F);  // Pulse LED rouge
  particleSensor.setPulseAmplitudeGreen(0);   // Désactive la LED verte

  // Initialisation du capteur de température
  sensors.begin();
  
  lcd.setCursor(0, 0);
  lcd.print("Lecture BPM...");
}

void loop() {
  // Lecture de la température
  sensors.requestTemperatures();
  float tempC = sensors.getTempCByIndex(0);  // Lecture de la température du premier capteur
  // Affichage de la température sur le moniteur série
  Serial.print("Température : ");
  Serial.print(tempC);
  Serial.println(" °C");

  // Lecture de la valeur IR du capteur MAX30102
  long irValue = particleSensor.getIR();  // Lire la valeur IR brute

  // Détection du battement
  if (irValue > lastIRValue + threshold && millis() - lastBeat > minInterval) {
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60.0 / (delta / 1000.0);
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

  // Mise à jour de la dernière valeur IR
  lastIRValue = irValue;

  // Affichage sur l'écran LCD
  lcd.setCursor(0, 1);
  lcd.print("BPM: ");
  lcd.print(beatAvg);
  lcd.print("   "); // Efface les caractères résiduels

  lcd.setCursor(0, 0);
  lcd.print("Temp: ");
  lcd.print(tempC);
  lcd.print(" C");  // Affiche la température

  // Affichage sur le moniteur série
  Serial.print("IR=");
  Serial.print(irValue);
  Serial.print(", BPM=");
  Serial.print(beatsPerMinute);
  Serial.print(", Moyenne BPM=");
  Serial.print(beatAvg);
  Serial.print(", Température=");
  Serial.print(tempC);
  Serial.println(" °C");

  delay(100);  // Pause pour limiter la fréquence d'échantillonnage
}
