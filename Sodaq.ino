#include <Wire.h>
#include <Sodaq_RN2483.h>

#define debugSerial SerialUSB
#define loraSerial Serial2

const uint8_t devEUI[8]  = {0x70, 0xB3, 0xD5, 0x7E, 0xD0, 0x06, 0xE8, 0x9C};
const uint8_t appEUI[8]  = {0x27, 0x07, 0x20, 0x03, 0x27, 0x07, 0x20, 0x03};
const uint8_t appKey[16] = {0x7F, 0x6C, 0x96, 0x3D, 0xCE, 0x01, 0x62, 0x5C, 0xBB, 0x5A, 0x3D, 0x7D, 0x7E, 0x51, 0x0D, 0xBE};

void setup() {
    debugSerial.begin(57600);
    Serial.begin(9600); // RX/TX avec la Leonardo
    loraSerial.begin(LoRaBee.getDefaultBaudRate());

    LoRaBee.setDiag(debugSerial);
    if (LoRaBee.initOTA(loraSerial, devEUI, appEUI, appKey, true)) {
        debugSerial.println("Connexion LoRa OK");
    } else {
        debugSerial.println("Echec connexion LoRa");
    }
}

void loop() {
    if (Serial.available() >= 3) { // Vérifie si au moins 3 octets sont reçus
        uint8_t data[3];
        Serial.readBytes(data, sizeof(data));  // Lire 3 octets envoyés par la Leonardo

        int bpm = data[0]; // BPM moyen
        float tempC = data[1]; // Température reçue
        uint8_t countChanges = data[2]; // Nombre de changements détectés

        debugSerial.print("Reçu - Temp: ");
        debugSerial.print(tempC);
        debugSerial.print("°C, BPM: ");
        debugSerial.print(bpm);
        debugSerial.print(", Changements: ");
        debugSerial.println(countChanges);

        // Envoi via LoRa
        if (LoRaBee.send(1, data, sizeof(data)) == NoError) {
            debugSerial.println("Transmission LoRa réussie");
        } else {
            debugSerial.println("Echec transmission LoRa");
        }
    }
}
