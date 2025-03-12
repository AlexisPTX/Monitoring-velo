#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include "MAX30105.h"
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Sodaq_RN2483.h>

// Initialisation des capteurs
MAX30105 particleSensor;
LiquidCrystal_I2C lcd(0x20, 20, 4);
OneWire oneWire(4);
DallasTemperature sensors(&oneWire);

// Configuration LoRa avec OTAA
#define debugSerial SerialUSB
#define loraSerial Serial2

// OTAA Keys - Utilisez vos propres clés!
const uint8_t devEUI[8]  = {0x70, 0xB3, 0xD5, 0x7E, 0xD0, 0x06, 0xE8, 0x9C};  // Remplacez par votre DevEUI
const uint8_t appEUI[8]  = {0x27, 0x07, 0x20, 0x03, 0x27, 0x07, 0x20, 0x03};  // Remplacez par votre AppEUI
const uint8_t appKey[16] = {0x7F, 0x6C, 0x96, 0x3D, 0xCE, 0x01, 0x62, 0x5C, 0xBB, 0x5A, 0x3D, 0x7D, 0x7E, 0x51, 0x0D, 0xBE};  // Remplacez par votre AppKey

const byte RATE_SIZE = 4;
byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;
float beatsPerMinute = 0;
int beatAvg = 50;
long lastIRValue = 0;

// Gestion des priorités pour éviter les blocages LoRa/BPM
unsigned long previousMillis = 0;
const unsigned long sensorInterval = 100;  // Intervalle pour les capteurs (100 ms)

void setup() {
    debugSerial.begin(57600);
    loraSerial.begin(LoRaBee.getDefaultBaudRate());
    lcd.init();
    lcd.backlight();
    sensors.begin();

    // Initialisation du capteur MAX30102
    if (!particleSensor.begin()) {
        debugSerial.println("MAX30102 non detecte");
        lcd.setCursor(0, 0);
        lcd.print("Capteur BPM absent");
        while (1);
    }

    lcd.setCursor(0, 0);
    lcd.print("Initialisation...");
    particleSensor.setup();
    particleSensor.setPulseAmplitudeRed(0x7F);
    particleSensor.setPulseAmplitudeGreen(0);

    // Initialisation LoRaWAN avec OTAA
    LoRaBee.setDiag(debugSerial);
    if (LoRaBee.initOTA(loraSerial, devEUI, appEUI, appKey, true)) {
        debugSerial.println("Connexion LoRa OK (OTAA)");
    } else {
        debugSerial.println("Echec connexion LoRa");
    }

    lcd.clear();
}

void readSensors() {
    // Lecture température
    sensors.requestTemperatures();
    float tempC = sensors.getTempCByIndex(0);

    // Lecture BPM
    long irValue = particleSensor.getIR();
    if (irValue > lastIRValue + 2000 && millis() - lastBeat > 300) {
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
    lastIRValue = irValue;

    // Affichage sur LCD
    lcd.setCursor(0, 0);
    lcd.print("Temp: ");
    lcd.print(tempC);
    lcd.print(" C ");
    lcd.setCursor(0, 1);
    lcd.print("BPM: ");
    lcd.print(beatAvg);
    lcd.print("   ");

    debugSerial.print("Temp: ");
    debugSerial.print(tempC);
    debugSerial.print(" C, BPM: ");
    debugSerial.println(beatAvg);
}

// Fonction pour mettre le module LoRa en veille
void loraSleep() {
    debugSerial.println("Mise en veille du module LoRa...");
    LoRaBee.sleep(); // Mettre le module en mode veille
    delay(100); // Petit délai pour s'assurer que la commande est prise en compte
}

// Fonction pour réveiller le module LoRa
void loraWakeUp() {
    debugSerial.println("Reveil du module LoRa...");
    LoRaBee.wakeUp(); // Réveiller le module
    delay(1000); // Laisser le temps au module de se réveiller complètement
}

void sendLoRaData() {
    // Réveiller le module LoRa avant l'envoi
    loraWakeUp();

    // Construction du payload
    uint8_t payload[3]; // 2 bytes pour BPM + 1 byte pour température
    payload[0] = beatAvg ; // Octet haut du BPM
    payload[1] = static_cast<uint8_t>(round(sensors.getTempCByIndex(0))); // Température

    // Envoyer les données
    bool messageSent = false;
    uint8_t retryCount = 0;

    while (!messageSent && retryCount < 3) {
        switch (LoRaBee.send(1, payload, sizeof(payload))) {
            case NoError:
                debugSerial.println("Transmission reussie.");
                messageSent = true;
                break;
            case Busy:
            case NoResponse:
                debugSerial.println("Module LoRa occupe ou pas de reponse.");
                retryCount++;
                delay(1000); // Attendre avant de réessayer
                break;
            default:
                debugSerial.println("Erreur inconnue.");
                retryCount++;
                delay(3000); // Attendre avant de réessayer
                break;
        }
    }

    // Remettre le module LoRa en veille après l'envoi
    loraSleep();
}

void loop() {
    unsigned long currentMillis = millis();

    // Lire les capteurs sans bloquer la boucle
    if (currentMillis - previousMillis >= sensorInterval) {
        previousMillis = currentMillis;
        readSensors();
    }

    // Envoi des données toutes les 5 secondes
    static unsigned long lastSendMillis = 0;
    if (currentMillis - lastSendMillis >= 15000) {
        lastSendMillis = currentMillis;
        sendLoRaData();
        delay(500);
    }
}
