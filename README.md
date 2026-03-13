# Music-Responsive Robot System

An **IoT-based robotic system** that analyzes the emotional characteristics of music and expresses them through animated robotic eyes.

The system connects an **Android application** with an **ESP32-based embedded device** using **Bluetooth Low Energy (BLE)**.  
When music is detected on the phone, the app analyzes the song's emotional features and sends the detected emotion to the robot, which responds with **visual eye animations on an OLED display**.

---

## 1. Project Overview

This project demonstrates an **end-to-end IoT system** combining:

- Mobile application development
- Embedded systems programming
- Real-time music emotion analysis
- Wireless communication between devices
- Visual robotic expression

The system detects the emotional tone of music and maps it to different **robotic eye expressions**, allowing the robot to visually react to music in real time.

---

## 2. System Workflow

1. A song is played on the user's device.
2. The Android application detects the currently playing music.
3. The app analyzes the song using a music analysis API.
4. The emotional characteristics of the music are classified.
5. The emotion label is transmitted to the ESP32 device using BLE.
6. The ESP32 processes the emotion data.
7. The robot displays a corresponding **eye animation on the OLED display**.

---

## 3. Features

- Real-time **music emotion detection**
- **Bluetooth Low Energy communication** between phone and robot
- Animated **robotic eye expressions**
- Large **music emotion classification dataset**
- OLED display visualization
- Interactive touch sensor support
- Modular and scalable architecture

---

## 4. Emotion Categories

The system classifies songs into **eight emotional states**:

1. Happy Energetic  
2. Happy Chill  
3. Cool Rap  
4. Aggressive Rap  
5. Sad  
6. Romantic Slow  
7. Romantic Happy  
8. Neutral  

Each emotion triggers a **different eye animation and behavior** on the robot.

---

## 5. Mobile Application

The Android application is responsible for **music detection, emotion analysis, and communication with the robot**.

### Technologies Used

- Kotlin
- Jetpack Compose
- Android BLE APIs
- OkHttp HTTP client
- RapidAPI music analysis
- Android Notification Listener Service

### Key Functions

1. Detect currently playing songs from music apps
2. Send song data to a music analysis API
3. Process audio features such as energy, valence, tempo, and danceability
4. Classify the emotional category of the song
5. Send the detected emotion to the ESP32 device via BLE

---

## 6. Embedded System (ESP32-C3)

The ESP32 microcontroller controls the robot's visual expression system.

### Hardware Components

- ESP32-C3 microcontroller
- OLED Display (SSD1306)
- Touch sensor
- I2C communication interface

### Software Features

1. BLE server for receiving emotion data
2. OLED display rendering
3. Robotic eye animation control
4. Emotion-based state machine
5. Smooth animation transitions

---

## 7. Robotic Eye Animation System

The robot uses the **FluxGarage RoboEyes library** to generate expressive robotic eyes.

Capabilities include:

- Multiple eye shapes and sizes
- Dynamic eye movements
- Emotion-based expressions
- Blinking and gaze animations
- Smooth transition between emotional states

More than **30 eye configurations** are implemented to represent different moods.

---

## 8. Communication Protocol

The mobile app and ESP32 communicate using **Bluetooth Low Energy (BLE)**.

The system uses:

- A custom BLE service
- A dedicated BLE characteristic for emotion data
- Real-time emotion updates
- Automatic reconnection handling

This allows the robot to respond quickly to changes in the music.

---

## 9. Technologies Used

### Mobile Development
- Kotlin
- Android
- Jetpack Compose

### Embedded Systems
- C++
- Arduino IDE
- ESP32 platform

### Hardware
- ESP32-C3
- OLED SSD1306 display
- Touch sensor

### Communication
- Bluetooth Low Energy (BLE)

### APIs and Networking
- RapidAPI
- REST APIs
- JSON parsing
- OkHttp

---

## 10. Project Structure

1. **android-app**
   1. MainActivity.kt  
   2. BleHelper.kt  
   3. AndroidManifest.xml  

2. **esp32-code**
   1. final_code.ino  
   2. esp32_corrected.ino  
   3. esp32_fixed.ino  

3. **data**
   1. data.csv  

4. **README**
   1. README.md
