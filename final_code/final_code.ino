#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <FluxGarage_RoboEyes.h>

// --- BLE LIBRARIES (Built-in) ---
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// --- PIN DEFINITIONS FOR ESP32-C3 ---
#define SDA_PIN 4
#define SCL_PIN 5

// --- OLED SETTINGS ---
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define i2c_Address 0x3c 

const int touch_side = 3;

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
RoboEyes<Adafruit_SSD1306> roboEyes0(display);
RoboEyes<Adafruit_SSD1306> roboEyes1(display);
RoboEyes<Adafruit_SSD1306> roboEyes2(display);
RoboEyes<Adafruit_SSD1306> roboEyes3(display);
RoboEyes<Adafruit_SSD1306> roboEyes4(display);
RoboEyes<Adafruit_SSD1306> roboEyes5(display);
RoboEyes<Adafruit_SSD1306> roboEyes6(display);
RoboEyes<Adafruit_SSD1306> roboEyes7(display);
RoboEyes<Adafruit_SSD1306> roboEyes8(display);
RoboEyes<Adafruit_SSD1306> roboEyes9(display);
RoboEyes<Adafruit_SSD1306> roboEyes10(display);
RoboEyes<Adafruit_SSD1306> roboEyes11(display);
RoboEyes<Adafruit_SSD1306> roboEyes12(display);
RoboEyes<Adafruit_SSD1306> roboEyes13(display);
RoboEyes<Adafruit_SSD1306> roboEyes14(display);
RoboEyes<Adafruit_SSD1306> roboEyes15(display);
RoboEyes<Adafruit_SSD1306> roboEyes16(display);
RoboEyes<Adafruit_SSD1306> roboEyes17(display);
RoboEyes<Adafruit_SSD1306> roboEyes18(display);
RoboEyes<Adafruit_SSD1306> roboEyes19(display);
RoboEyes<Adafruit_SSD1306> roboEyes20(display);
RoboEyes<Adafruit_SSD1306> roboEyes21(display);
RoboEyes<Adafruit_SSD1306> roboEyes22(display);
RoboEyes<Adafruit_SSD1306> roboEyes23(display);
RoboEyes<Adafruit_SSD1306> roboEyes24(display);
RoboEyes<Adafruit_SSD1306> roboEyes25(display);
RoboEyes<Adafruit_SSD1306> roboEyes26(display);
RoboEyes<Adafruit_SSD1306> roboEyes27(display);
RoboEyes<Adafruit_SSD1306> roboEyes28(display);
RoboEyes<Adafruit_SSD1306> roboEyes29(display);
RoboEyes<Adafruit_SSD1306> roboEyes30(display);

// --- BLE UUIDs ---
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// --- VARIABLES ---
// We use a flag so we don't crash the BLE connection by doing long animations inside the callback
volatile int receivedEmotionID = -1; 
bool deviceConnected = false;

// This class inherits from the library's base class "BLECharacteristicCallbacks"
class MyCallbacks: public BLECharacteristicCallbacks{
    // This function runs AUTOMATICALLY whenever someone (your phone) writes data to the robot
    void onWrite(BLECharacteristic *pCharacteristic){
      // pCharacteristic->getValue() grabs whatever string the phone sent (e.g., "1")
      String value = pCharacteristic->getValue();

      // Ensure the message isn't empty
      if (value.length() > 0) {
        char c = value[0];
        receivedEmotionID = c - '0';
        Serial.print("Received Emotion ID: ");
        Serial.println(receivedEmotionID);
    }
  }
};

class MyServerCallbacks: public BLEServerCallbacks{
    // Runs when phone connects
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;       // Set our global flag to true
      Serial.println("Phone Connected!");
    };

    // Runs when phone disconnects
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;      // Set flag to false
      Serial.println("Phone Disconnected. Restarting Scan...");
      
      // CRITICAL LINE: Restart Advertising!
      // If we don't do this, once you disconnect, the robot becomes "invisible" 
      // and you can never connect to it again without resetting the board.
      BLEDevice::startAdvertising(); 
    }
};


void setup() {
  Serial.begin(115200);
  delay(500);
  Wire.begin(SDA_PIN, SCL_PIN);

  pinMode(touch_side, INPUT);

  if(!display.begin(SSD1306_SWITCHCAPVCC, i2c_Address)) {
    Serial.println("SSD1306 allocation failed");
    while(true);
  }

  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.display();

  roboEyes0.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes1.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes2.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes3.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes4.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);

  roboEyes5.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes6.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes7.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes8.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);

  roboEyes9.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes10.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes11.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes12.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);

  roboEyes13.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes14.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes15.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes16.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);

  
  roboEyes17.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes18.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes19.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes20.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);

  
  roboEyes21.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes22.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes23.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes24.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  
  roboEyes25.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes26.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes27.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes28.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes29.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  roboEyes30.begin(SCREEN_WIDTH, SCREEN_HEIGHT, 100);
  

  roboEyes0.setMood(DEFAULT);
  roboEyes0.setCuriosity(OFF);
  roboEyes0.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes0.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes0.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes0.setSpacebetween(10); // int space -> can also be negative

  //AGGRESSIVE RAPPING
  roboEyes1.setMood(DEFAULT);
  roboEyes1.setCuriosity(OFF);
  roboEyes1.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes1.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes1.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes1.setSpacebetween(10); // int space -> can also be negative

  roboEyes2.setMood(DEFAULT);
  roboEyes2.setCuriosity(OFF);
  roboEyes2.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes2.setHeight(16,16); // byte leftEye, byte rightEye
  roboEyes2.setBorderradius(1, 1); // byte leftEye, byte rightEye
  roboEyes2.setSpacebetween(11); // int space -> can also be negative

  roboEyes3.setMood(DEFAULT); 
  roboEyes3.setCuriosity(OFF);
  roboEyes3.setWidth(30, 36); // byte leftEye, byte rightEye
  roboEyes3.setHeight(36, 20); // byte leftEye, byte rightEye
  roboEyes3.setBorderradius(2,2); // byte leftEye, byte rightEye
  roboEyes3.setSpacebetween(10); // int space -> can also be negative

  roboEyes4.setMood(DEFAULT);
  roboEyes4.setCuriosity(OFF);
  roboEyes4.setWidth(36, 30); // byte leftEye, byte rightEye
  roboEyes4.setHeight(20, 36); // byte leftEye, byte rightEye
  roboEyes4.setBorderradius(2,2); // byte leftEye, byte rightEye
  roboEyes4.setSpacebetween(10); // int space -> can also be negative

  //CHILL RAPPING
  roboEyes5.setMood(DEFAULT);
  roboEyes5.setCuriosity(OFF);
  roboEyes5.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes5.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes5.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes5.setSpacebetween(10); // int space -> can also be negative

  roboEyes6.setMood(DEFAULT);
  roboEyes6.setCuriosity(OFF);
  roboEyes6.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes6.setHeight(16,16); // byte leftEye, byte rightEye
  roboEyes6.setBorderradius(4, 4); // byte leftEye, byte rightEye
  roboEyes6.setSpacebetween(11); // int space -> can also be negative

  roboEyes7.setMood(DEFAULT); 
  roboEyes7.setCuriosity(OFF);
  roboEyes7.setWidth(30, 36); // byte leftEye, byte rightEye
  roboEyes7.setHeight(36, 28); // byte leftEye, byte rightEye
  roboEyes7.setBorderradius(7,7); // byte leftEye, byte rightEye
  roboEyes7.setSpacebetween(10); // int space -> can also be negative

  roboEyes8.setMood(DEFAULT);
  roboEyes8.setCuriosity(OFF);
  roboEyes8.setWidth(36, 30); // byte leftEye, byte rightEye
  roboEyes8.setHeight(28, 36); // byte leftEye, byte rightEye
  roboEyes8.setBorderradius(7,7); // byte leftEye, byte rightEye
  roboEyes8.setSpacebetween(10); // int space -> can also be negative

  //SAD
  roboEyes9.setMood(DEFAULT);
  roboEyes9.setCuriosity(OFF);
  roboEyes9.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes9.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes9.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes9.setSpacebetween(10); // int space -> can also be negative
  
  roboEyes10.setMood(DEFAULT);
  roboEyes10.setCuriosity(OFF);
  roboEyes10.setWidth(18, 18); // byte leftEye, byte rightEye
  roboEyes10.setHeight(40, 40); // byte leftEye, byte rightEye
  roboEyes10.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes10.setSpacebetween(11); // int space -> can also be negative

  roboEyes11.setMood(DEFAULT);
  roboEyes11.setCuriosity(OFF);
  roboEyes11.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes11.setHeight(13, 13); // byte leftEye, byte rightEye
  roboEyes11.setBorderradius(2, 2); // byte leftEye, byte rightEye
  roboEyes11.setSpacebetween(10); // int space -> can also be negative

  //CHILL HAPPY
  roboEyes12.setMood(DEFAULT);
  roboEyes12.setCuriosity(OFF);
  roboEyes12.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes12.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes12.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes12.setSpacebetween(10); // int space -> can also be negative

  roboEyes13.setMood(DEFAULT);
  roboEyes13.setCuriosity(OFF);
  roboEyes13.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes13.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes13.setBorderradius(10, 10); // byte leftEye, byte rightEye
  roboEyes13.setSpacebetween(10); // int space -> can also be negative

  roboEyes14.setMood(DEFAULT);
  roboEyes14.setCuriosity(OFF);
  roboEyes14.setWidth(30, 30); // byte leftEye, byte rightEye
  roboEyes14.setHeight(30, 30); // byte leftEye, byte rightEye
  roboEyes14.setBorderradius(20, 20); // byte leftEye, byte rightEye
  roboEyes14.setSpacebetween(10); // int space -> can also be negative

  //ENERGETIC HAPPY
  roboEyes15.setMood(DEFAULT);
  roboEyes15.setCuriosity(OFF);
  roboEyes15.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes15.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes15.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes15.setSpacebetween(10); // int space -> can also be negative

  roboEyes16.setMood(DEFAULT);
  roboEyes16.setCuriosity(OFF);
  roboEyes16.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes16.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes16.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes16.setSpacebetween(10); // int space -> can also be negative

  roboEyes17.setMood(DEFAULT);
  roboEyes17.setCuriosity(OFF);
  roboEyes17.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes17.setHeight(20, 20); // byte leftEye, byte rightEye
  roboEyes17.setBorderradius(10, 10); // byte leftEye, byte rightEye
  roboEyes17.setSpacebetween(10); // int space -> can also be negative

  roboEyes18.setMood(DEFAULT);
  roboEyes18.setCuriosity(OFF);
  roboEyes18.setWidth(50 , 50 ); // byte leftEye, byte rightEye
  roboEyes18.setHeight(50, 50); // byte leftEye, byte rightEye
  roboEyes18.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes18.setSpacebetween(15); // int space -> can also be negative

  //ROMANTIC SLOW
  roboEyes19.setMood(DEFAULT);
  roboEyes19.setCuriosity(OFF);
  roboEyes19.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes19.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes19.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes19.setSpacebetween(10); // int space -> can also be negative

    //heart
  roboEyes20.setCuriosity(OFF);
  roboEyes20.setWidth(25, 25); // byte leftEye, byte rightEye
  roboEyes20.setHeight(40, 40); // byte leftEye, byte rightEye
  roboEyes20.setBorderradius(30,30); // byte leftEye, byte rightEye
  roboEyes20.setSpacebetween(-6); // int space -> can also be negative
  roboEyes20.setMood(TIRED);

  roboEyes21.setMood(DEFAULT);
  roboEyes21.setCuriosity(OFF);
  roboEyes21.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes21.setHeight(20, 20); // byte leftEye, byte rightEye
  roboEyes21.setBorderradius(7, 7); // byte leftEye, byte rightEye
  roboEyes21.setSpacebetween(10); // int space -> can also be negative

  //ROMANTIC ENERGETIC
  roboEyes22.setMood(DEFAULT);
  roboEyes22.setCuriosity(OFF);
  roboEyes22.setWidth(36, 36); // byte leftEye, byte rightEye
  roboEyes22.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes22.setBorderradius(8, 8); // byte leftEye, byte rightEye
  roboEyes22.setSpacebetween(10); // int space -> can also be negative

    //heart
  roboEyes23.setCuriosity(OFF);
  roboEyes23.setWidth(25, 25); // byte leftEye, byte rightEye
  roboEyes23.setHeight(40, 40); // byte leftEye, byte rightEye
  roboEyes23.setBorderradius(30,30); // byte leftEye, byte rightEye
  roboEyes23.setSpacebetween(-6); // int space -> can also be negative
  roboEyes23.setMood(TIRED);

    //flower
  roboEyes24.setCuriosity(OFF);
  roboEyes24.setWidth(25, 25); // byte leftEye, byte rightEye
  roboEyes24.setHeight(40, 40); // byte leftEye, byte rightEye
  roboEyes24.setBorderradius(30,30); // byte leftEye, byte rightEye
  roboEyes24.setSpacebetween(-20); // int space -> can also be negative
  roboEyes24.setMood(ANGRY);

  roboEyes25.setMood(DEFAULT);
  roboEyes25.setCuriosity(OFF);
  roboEyes25.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes25.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes25.setBorderradius(12, 12); // byte leftEye, byte rightEye
  roboEyes25.setSpacebetween(10); // int space -> can also be negative

  roboEyes26.setMood(DEFAULT);
  roboEyes26.setCuriosity(OFF);
  roboEyes26.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes26.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes26.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes26.setSpacebetween(10); // int space -> can also be negative

  roboEyes27.setMood(DEFAULT);
  roboEyes27.setCuriosity(OFF);
  roboEyes27.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes27.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes27.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes27.setSpacebetween(10); // int space -> can also be negative

  roboEyes28.setMood(DEFAULT);
  roboEyes28.setCuriosity(OFF);
  roboEyes28.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes28.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes28.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes28.setSpacebetween(10); // int space -> can also be negative

  roboEyes29.setMood(DEFAULT);
  roboEyes29.setCuriosity(OFF);
  roboEyes29.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes29.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes29.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes29.setSpacebetween(10); // int space -> can also be negative

  roboEyes30.setMood(DEFAULT);
  roboEyes30.setCuriosity(OFF);
  roboEyes30.setWidth(20, 20); // byte leftEye, byte rightEye
  roboEyes30.setHeight(36, 36); // byte leftEye, byte rightEye
  roboEyes30.setBorderradius(14, 14); // byte leftEye, byte rightEye
  roboEyes30.setSpacebetween(10); // int space -> can also be negative






  // 1. START BLE
  Serial.println("Starting BLE...");
  display.println("starting ble");

  // Set the name that shows up on your phone's Bluetooth scan
  BLEDevice::init("MOCHI_ROBOT");

  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // 3. CREATE SERVICE (The Folder)
  // Create the "Emotions" folder using your specific UUID
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // 4. CREATE CHARACTERISTIC (The Paper)
  // Create the specific sheet of paper inside the folder where we write the number.
  // PROPERTY_READ  = Phone can read the current number
  // PROPERTY_WRITE = Phone can write a new number (this is what we need most)
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID,BLECharacteristic::PROPERTY_READ|BLECharacteristic::PROPERTY_WRITE);

  // 5. ATTACH THE LISTENER
  // Tell this Characteristic to use the "Listener" we wrote in Step 1.
  // Now, when someone writes to this characteristic, "onWrite" will trigger.
  pCharacteristic->setCallbacks(new MyCallbacks());

  // Set an initial value of "0" (Neutral) just so it's not empty
  pCharacteristic->setValue("0");

  // 6. GO LIVE
  // Start the service so it actually works
  pService->start();

  // 7. START ADVERTISING (Shouting)
  // This sets up the radio signals to tell nearby phones "I am here!"
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID); // Tells phones "I offer the Robot Service"
  pAdvertising->setScanResponse(true);

  // Start shouting now!
  BLEDevice::startAdvertising();
  
  Serial.println("Ready! Waiting for command...");

  display.display();
}
unsigned long movement_ld_time = 0;
bool is_moving_ld = false;
bool left = false;
bool right = false;
bool d = false;

unsigned long movement_dr_time = 0;
bool is_moving_dr = false;

unsigned long movement_ud_time = 0;
bool is_moving_ud = false;
bool up = false;
bool down = false;

unsigned long crap_start_time = 0;
bool is_chill_rapping = false;
int chill_rap_stage = 0;

unsigned long arap_start_time = 0;
bool is_aggressive_rapping = false;
int aggressive_rap_stage = 0;
int active_eye = 1;

unsigned long sad_start_time = 0;
bool is_sad = false;
int sad_stage = 0;

bool is_ehappy = false;
unsigned long ehappy_start_time = 0;
int ehappy_stage = 0;

bool is_chappy = false;
unsigned long chappy_start_time = 0;
int chappy_stage = 0;

bool is_sromantic = false;
unsigned long sromantic_start_time = 0;
int sromantic_stage = 0;

bool is_hromantic = false;
unsigned long hromantic_start_time = 0;
int hromantic_stage = 0;

bool is_touch_moving = false;
unsigned long touch_start_time = 0;
int touch_stage = 0;

int count = 0;
int temp_id = -1;
int current_emotion = 0;  // Store current emotion to return to after touch

bool move_ld(int id){
  movement_ld_time = millis();
  is_moving_ld = true;
  left = true;
  d = false;
    if(id == 7){
      roboEyes7.setPosition(E);
      roboEyes7.setMood(HAPPY);
      roboEyes7.setAutoblinker(ON, 4, 2);
    }
    else if(id == 6){
      roboEyes6.setPosition(E);
      roboEyes6.setMood(DEFAULT);
      roboEyes6.setAutoblinker(ON, 4, 2);
    }
    else if(id == 8){
      roboEyes8.setPosition(E);
      roboEyes8.setMood(HAPPY);
      roboEyes8.setAutoblinker(ON, 4, 2);
    }
    else if(id == 12){
      roboEyes12.setPosition(E);
      roboEyes12.setMood(HAPPY);
      roboEyes12.setAutoblinker(ON, 4, 2);
    }
    else if(id == 14){
      roboEyes14.setPosition(E);
      roboEyes14.setMood(HAPPY);
      roboEyes14.setAutoblinker(ON, 4, 2);
    }
    else if(id == 16){
      roboEyes16.setPosition(E);
      roboEyes16.setMood(HAPPY);
      roboEyes16.setAutoblinker(ON, 4, 2);
    }
    else if(id == 19){
      roboEyes19.setPosition(E);
      roboEyes19.setMood(HAPPY);
      roboEyes19.setAutoblinker(ON, 4, 2);
    }
    else if(id == 21){
      roboEyes21.setPosition(E);
      roboEyes21.setMood(HAPPY);
      roboEyes21.setAutoblinker(ON, 4, 2);
    }
    else if(id == 22){
      roboEyes22.setPosition(E);
      roboEyes22.setMood(HAPPY);
      roboEyes22.setAutoblinker(ON, 4, 2);
    }
    return true;
}

bool move_dr(int id){
  movement_dr_time = millis();
  is_moving_dr = true;
  d = true;
  right = false;
    if(id == 7){
      roboEyes7.setPosition(N);
      roboEyes7.setMood(HAPPY);
      roboEyes7.setAutoblinker(ON, 4, 2);
    }
    else if(id == 6){
      roboEyes6.setPosition(N);
      roboEyes6.setMood(DEFAULT);
      roboEyes6.setAutoblinker(ON, 4, 2);
    }
    else if(id == 8){
      roboEyes8.setPosition(N);
      roboEyes8.setMood(HAPPY);
      roboEyes8.setAutoblinker(ON, 4, 2);
    }
    else if(id == 12){
      roboEyes12.setPosition(N);
      roboEyes12.setMood(HAPPY);
      roboEyes12.setAutoblinker(ON, 4, 2);
    }
    else if(id == 14){
      roboEyes14.setPosition(N);
      roboEyes14.setMood(HAPPY);
      roboEyes14.setAutoblinker(ON, 4, 2);
    }
    else if(id == 16){
      roboEyes16.setPosition(N);
      roboEyes16.setMood(HAPPY);
      roboEyes16.setAutoblinker(ON, 4, 2);
    }
    else if(id == 19){
      roboEyes19.setPosition(N);
      roboEyes19.setMood(HAPPY);
      roboEyes19.setAutoblinker(ON, 4, 2);
    }
    else if(id == 21){
      roboEyes21.setPosition(N);
      roboEyes21.setMood(HAPPY);
      roboEyes21.setAutoblinker(ON, 4, 2);
    }
    else if(id == 22){
      roboEyes22.setPosition(N);
      roboEyes22.setMood(HAPPY);
      roboEyes22.setAutoblinker(ON, 4, 2);
    }
    return true;
}

bool move_ud(int id){
  movement_ud_time = millis();
  is_moving_ud = true;
  up = true;
  down = false;
    if(id == 7){
      roboEyes7.setPosition(N);
      roboEyes7.setMood(HAPPY);
      roboEyes7.setAutoblinker(ON, 4, 2);
    }
    else if(id == 6){
      roboEyes6.setPosition(N);
      roboEyes6.setMood(DEFAULT);
      roboEyes6.setAutoblinker(ON, 4, 2);
    }
    else if(id == 8){
      roboEyes8.setPosition(N);
      roboEyes8.setMood(HAPPY);
      roboEyes8.setAutoblinker(ON, 4, 2);
    }
    else if(id == 9){
      roboEyes9.setPosition(N);
      roboEyes9.setMood(TIRED);
      roboEyes9.setAutoblinker(ON, 4, 2);
    }
    else if(id == 12){
      roboEyes12.setPosition(N);
      roboEyes12.setMood(HAPPY);
      roboEyes12.setAutoblinker(ON, 4, 2);
    }
    else if(id == 14){
      roboEyes14.setPosition(N);
      roboEyes14.setMood(HAPPY);
      roboEyes14.setAutoblinker(ON, 4, 2);
    }
    else if(id == 16){
      roboEyes16.setPosition(N);
      roboEyes16.setMood(HAPPY);
      roboEyes16.setAutoblinker(ON, 4, 2);
    }
    else if(id == 19){
      roboEyes19.setPosition(N);
      roboEyes19.setMood(HAPPY);
      roboEyes19.setAutoblinker(ON, 4, 2);
    }
    else if(id == 21){
      roboEyes21.setPosition(N);
      roboEyes21.setMood(HAPPY);
      roboEyes21.setAutoblinker(ON, 4, 2);
    }
    else if(id == 22){
      roboEyes22.setPosition(N);
      roboEyes22.setMood(HAPPY);
      roboEyes22.setAutoblinker(ON, 4, 2);
    }
    return true;
}

void updateRobotMood(int id) {
  // Store current emotion
  current_emotion = id;
  
  is_aggressive_rapping = false;
  is_chill_rapping = false;
  is_moving_ld = false;
  is_moving_dr = false;
  is_moving_ud = false;
  is_sad = false;
  is_ehappy = false;
  is_chappy = false;
  is_sromantic = false;
  is_hromantic = false;
  is_touch_moving = false;

  count = 0; 
  aggressive_rap_stage = 0;
  chill_rap_stage = 0;
  sad_stage = 0;
  ehappy_stage = 0;
  chappy_stage = 0;
  sromantic_stage = 0;
  hromantic_stage = 0;
  touch_stage = 0;
  
  // Reset previous state
  roboEyes1.setMood(DEFAULT);
  roboEyes1.setCuriosity(OFF);
  display.clearDisplay();
  display.setCursor(0, 0); 
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  switch(id) {
    case 1: // HAPPY ENERGETIC
      Serial.println("Mood: Happy Energetic");
      happy_energetic();
      break;

    case 2: // HAPPY CHILL
      Serial.println("Mood: Happy Chill");
      //display.println("happy chill");
      happy_chill();
      break;

    case 3: // COOL RAP
      Serial.println("Mood: Cool");
      chill_rap();
      break;

    case 4: // AGGRESSIVE RAP
      Serial.println("Mood: Aggressive");
      //display.println("aggressive");
      aggressive_rap();
      break;

    case 5: // SAD
      Serial.println("Mood: Sad");
      sad();
      break;

    case 6: // ROMANTIC SLOW
      Serial.println("Mood: Romantic");
      //display.println("romantic");
      romantic_slow();
      break;

    case 7: // ROMANTIC HAPPY
      Serial.println("Mood: Romantic Happy");
      //display.println("romantic happy");
      romantic_happy();
      break;

    default: // NEUTRAL or 0
      Serial.println("Mood: Neutral");
      //display.println("neutral");
      roboEyes1.setMood(DEFAULT);
      break;

  }
  display.display(); 
}

bool side_on = false;

void touch_move(){
  is_touch_moving = true;
  touch_start_time = millis();
  touch_stage = 0;
  active_eye = 0;

  roboEyes0.setMood(HAPPY);
  roboEyes0.setAutoblinker(ON,2,1);
}

void return_to_current_emotion(){
  // Return to whatever emotion was playing before touch
  updateRobotMood(current_emotion);
}

void aggressive_rap(){
  is_aggressive_rapping = true;
  arap_start_time = millis();
  aggressive_rap_stage = 0;
  active_eye = 1;
  
  roboEyes1.setMood(ANGRY);
  roboEyes1.setAutoblinker(ON, 2, 2);
  roboEyes1.setIdleMode(ON, 1, 1);
  //stays normal and blinks and looks around
}

void chill_rap(){
  is_chill_rapping = true;
  crap_start_time = millis();
  chill_rap_stage = 0;
  active_eye = 5;

  roboEyes5.setMood(ANGRY);
  roboEyes5.setAutoblinker(ON, 2, 2);
  roboEyes5.setIdleMode(ON, 1, 1);
}

void sad(){
  is_sad = true;
  sad_start_time = millis();
  sad_stage = 0;
  active_eye = 9;

  roboEyes9.setMood(DEFAULT);
  roboEyes9.setAutoblinker(ON, 3, 2);
  roboEyes9.setIdleMode(ON, 3, 2);
}

void happy_chill(){
  is_chappy = true;
  chappy_start_time = millis();
  chappy_stage = 0;
  active_eye = 12;

  roboEyes12.setMood(HAPPY);
  roboEyes12.setAutoblinker(ON, 3, 2);
  roboEyes12.setIdleMode(ON, 2, 1);
}
  
void happy_energetic(){
  is_ehappy = true;
  ehappy_start_time = millis();
  ehappy_stage = 0;
  active_eye = 15;

  roboEyes15.setMood(HAPPY);
  roboEyes15.setAutoblinker(ON, 2, 2);
  roboEyes15.setIdleMode(ON, 1, 1);
}

void romantic_slow(){
  is_sromantic = true;
  sromantic_start_time = millis();
  sromantic_stage = 0;
  active_eye = 19;

  roboEyes19.setMood(HAPPY);
  roboEyes19.setAutoblinker(ON, 2, 2);
  roboEyes19.setIdleMode(ON, 1, 1);
}

void romantic_happy(){
  is_hromantic = true;
  hromantic_start_time = millis();
  hromantic_stage = 0;
  active_eye = 22;

  roboEyes22.setMood(HAPPY);
  roboEyes22.setAutoblinker(ON, 2, 2);
  roboEyes22.setIdleMode(ON, 1, 1);
}

void loop() {
  // 1. UPDATE ANIMATION
  // This MUST run fast and often. If this stops, the eyes freeze.
  if(active_eye == 1) roboEyes1.update();
  else if(active_eye == 2)roboEyes2.update();
  else if(active_eye == 3)roboEyes3.update();
  else if(active_eye == 4)roboEyes4.update();
  else if(active_eye == 5)roboEyes5.update();
  else if(active_eye == 6)roboEyes6.update();
  else if(active_eye == 7)roboEyes7.update();
  else if(active_eye == 8)roboEyes8.update();
  else if(active_eye == 9)roboEyes9.update();
  else if(active_eye == 10)roboEyes10.update();
  else if(active_eye == 11)roboEyes11.update();
  else if(active_eye == 12)roboEyes12.update();
  else if(active_eye == 13)roboEyes13.update();
  else if(active_eye == 14)roboEyes14.update();
  else if(active_eye == 15)roboEyes15.update();
  else if(active_eye == 16)roboEyes16.update();
  else if(active_eye == 17)roboEyes17.update();
  else if(active_eye == 18)roboEyes18.update();
  else if(active_eye == 19)roboEyes19.update();
  else if(active_eye == 20)roboEyes20.update();
  else if(active_eye == 21)roboEyes21.update();
  else if(active_eye == 22)roboEyes22.update();
  else if(active_eye == 23)roboEyes23.update();
  else if(active_eye == 24)roboEyes24.update();
  else if(active_eye == 25)roboEyes25.update();
  else if(active_eye == 26)roboEyes26.update();
  else if(active_eye == 27)roboEyes27.update();
  else if(active_eye == 28)roboEyes28.update();
  else if(active_eye == 29)roboEyes29.update();
  else if(active_eye == 30)roboEyes30.update();
  else if(active_eye == 0)roboEyes0.update();
  else roboEyes1.update();

  int touch_sensor_state = digitalRead(touch_side);

  if(touch_sensor_state == HIGH){
        // Touch is pressed - start touch sequence if not already started
        if (!is_touch_moving) {
          touch_move();
        }
        
        unsigned elapsed = millis() - touch_start_time;
        if(touch_stage == 0 && elapsed>=50){
          active_eye = 0;
          roboEyes0.setPosition(E);
          touch_stage = 1;
        }

        if(touch_stage == 1 && elapsed >= 500){
          roboEyes0.setPosition(NE);
          touch_stage = 2;
        }

        if(touch_stage == 2 && elapsed >=1000){
          roboEyes0.setPosition(SE);
          touch_stage = 3;
        }

        if(touch_stage == 3 && elapsed>= 1500){
          display.clearDisplay();
          active_eye = 0;
          // Keep showing touch animation until released
        }
    }

  else{
      // Touch is released
      if (is_touch_moving) {
        // Was showing touch animation, now return to previous emotion
        is_touch_moving = false;
        return_to_current_emotion();
      }
      
      side_on = false;
          //aggressive rapping
        if(is_aggressive_rapping){
          unsigned elapsed = millis() - arap_start_time;
          if(aggressive_rap_stage == 0 && elapsed >= 1000){
            active_eye = 1;
            roboEyes1.setMood(ANGRY);
            aggressive_rap_stage = 1;
            roboEyes1.setAutoblinker(ON, 2, 2);
            roboEyes1.setIdleMode(ON, 1, 1);
          }

          if(aggressive_rap_stage == 1 && elapsed >= 5000){
            roboEyes1.setAutoblinker(OFF);
            roboEyes1.setIdleMode(ON, 0.5, 0.5);
            aggressive_rap_stage = 2;
          }

          if(aggressive_rap_stage == 2 && elapsed >= 7000){
            display.clearDisplay();
            active_eye = 2;
            roboEyes2.setMood(DEFAULT);
            roboEyes2.setAutoblinker(ON, 2, 2);
            roboEyes2.setIdleMode(ON, 1, 1);
            aggressive_rap_stage = 3;
          }

          if(aggressive_rap_stage == 3 && elapsed >= 10000){
            roboEyes2.setMood(ANGRY);
            roboEyes2.setAutoblinker(OFF);
            roboEyes2.setIdleMode(ON, 0.5, 0.5);
            aggressive_rap_stage = 4;
          }

          if(aggressive_rap_stage == 4 && elapsed >= 12000){
            display.clearDisplay();
            active_eye = 3;
            roboEyes3.setMood(ANGRY);
            roboEyes3.setIdleMode(ON, 1, 1);
            aggressive_rap_stage = 5;
          }

          if(aggressive_rap_stage == 5 && elapsed >= 15000){
            display.clearDisplay();
            active_eye = 4;
            roboEyes4.setMood(ANGRY);
            roboEyes4.setIdleMode(ON, 1, 1);
            aggressive_rap_stage = 6;
          }

          if(aggressive_rap_stage == 6 && elapsed >= 18000){
            display.clearDisplay();
            active_eye = 1;
            roboEyes1.setMood(ANGRY);
            aggressive_rap_stage = 7;
            roboEyes1.setAutoblinker(ON, 2, 2);
            roboEyes1.setIdleMode(ON, 1, 1);
          }

          if(aggressive_rap_stage == 7 && elapsed >= 21000){
            roboEyes1.setAutoblinker(OFF);
            roboEyes1.setIdleMode(ON, 0.5, 0.5);
            aggressive_rap_stage = 8;
          }
          
          if(aggressive_rap_stage == 8 && elapsed >= 24000){
            display.clearDisplay();
            active_eye = 4;
            roboEyes4.setIdleMode(ON, 0.5, 0.5);
            aggressive_rap_stage = 9;
          }

          if(aggressive_rap_stage == 9 && elapsed >= 27000){
            display.clearDisplay();
            active_eye = 3;
            roboEyes3.setMood(ANGRY);
            roboEyes3.setIdleMode(ON, 0.5, 0.5);
            aggressive_rap_stage = 10;
          }

          if(aggressive_rap_stage == 10 && elapsed >= 30000){
            display.clearDisplay();
            active_eye = 1;
            aggressive_rap();
          }
        }

        //chill rapping
        if(is_chill_rapping){
          unsigned long elapsed = millis() - crap_start_time;
          if(is_moving_ld){
            unsigned long elapsed_ld = millis() - movement_ld_time;
            
            if(elapsed_ld >= 500 && elapsed_ld < 1000 && !d){
              if(active_eye == 7){
                roboEyes7.setPosition(W);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(W);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(W);
              }
              d = true;
            }

            if(elapsed_ld >= 1000){
              count++;
              movement_ld_time = millis();
              d = false;

              if(active_eye == 7){
                roboEyes7.setPosition(E);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(E);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(E);
              }
              }
          }

          if(is_moving_dr){
            unsigned long elapsed_dr = millis() - movement_dr_time;
            
            if(elapsed_dr >= 250 && elapsed_dr < 500 && !right){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              right = true;
            }

            if(elapsed_dr >= 500){
              count++;
              movement_dr_time = millis();
              right = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              }
          }
        

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 400 && elapsed_ud < 800 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 800){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
            }
          }

          if(chill_rap_stage == 0 && elapsed >= 2000){
            active_eye = 5;
            roboEyes5.setMood(ANGRY);
            roboEyes5.setAutoblinker(ON, 2, 2);
            roboEyes5.setIdleMode(ON, 0.5, 0.5);
            chill_rap_stage = 1;
          }

          if(chill_rap_stage == 1 && elapsed >= 5000){
            display.clearDisplay();
            active_eye = 6;
            roboEyes6.setMood(DEFAULT);
            roboEyes6.setAutoblinker(ON, 2,1);
            roboEyes6.setIdleMode(ON,1,1);
            chill_rap_stage = 2;
          }

          if(chill_rap_stage == 2 && elapsed >= 8000){
            roboEyes6.setIdleMode(OFF);
            if(!is_moving_ld && count == 0){
              temp_id = move_ld(active_eye);
            }
            if(count >= 3){
              chill_rap_stage = 3;
              is_moving_ld = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 3 && elapsed >= 11000){
            roboEyes6.setIdleMode(OFF);
            if(!is_moving_dr && count == 0){
              temp_id = move_dr(active_eye);
            }
            if(count >= 6){
              chill_rap_stage = 4;
              is_moving_dr = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 4 && elapsed >= 14000 ){
            display.clearDisplay();
            active_eye = 7;
            roboEyes7.setMood(HAPPY);
            roboEyes7.setAutoblinker(ON,2,1);
            roboEyes7.setIdleMode(ON,1,1);
            chill_rap_stage = 5;      
          }

          if(chill_rap_stage == 5 && elapsed >= 16000){
            roboEyes7.setIdleMode(OFF);
            if(!is_moving_ld && count == 0){
              temp_id = move_ld(active_eye);
            }
            if(count >= 3){
              chill_rap_stage = 6;
              is_moving_ld = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 6 && elapsed >= 19000){
            roboEyes7.setIdleMode(OFF);
            if(!is_moving_dr && count == 0){
              temp_id = move_dr(active_eye);
            }
            if(count >= 6){
              chill_rap_stage = 7;
              is_moving_dr = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 7 && elapsed >= 22000){ 
            display.clearDisplay();
            active_eye = 8;
            roboEyes8.setMood(HAPPY);
            roboEyes8.setAutoblinker(ON,2,1);
            roboEyes8.setIdleMode(ON,1,1);
            chill_rap_stage = 8;
          }

          if(chill_rap_stage == 8 && elapsed >= 24000){
            roboEyes8.setIdleMode(OFF);
            if(!is_moving_ld && count == 0){
              temp_id = move_ld(active_eye);
            }
            if(count >= 3){
              chill_rap_stage = 9;
              is_moving_ld = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 9 && elapsed >= 27000){
            roboEyes8.setIdleMode(OFF);
            if(!is_moving_dr && count == 0){
              temp_id = move_dr(active_eye);
            }
            if(count >= 6){
              chill_rap_stage = 10;
              is_moving_dr = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 10 && elapsed >= 30000){
            display.clearDisplay();
            active_eye = 6;
            roboEyes6.setMood(ANGRY);
            roboEyes6.setAutoblinker(OFF);
            roboEyes6.setIdleMode(OFF);
            if(!is_moving_ud && count == 0){
              temp_id = move_ud(active_eye);
            }
            if(count >= 12){
              chill_rap_stage = 11;
              is_moving_ud = false;
              count = 0;
            }
          }

          if(chill_rap_stage == 11 && elapsed >= 39600){
            display.clearDisplay();
            active_eye = 5;
            chill_rap();
          }
        }

        //sad
        if(is_sad){
          unsigned elapsed = millis() - sad_start_time;

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 500 && elapsed_ud < 1000 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 1000){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(N);
              }
            }
          }
          
          if(sad_stage == 0 && elapsed >=2000){
            active_eye = 9;
            roboEyes9.setMood(TIRED);
            sad_stage = 1;
            roboEyes9.setAutoblinker(ON, 3, 2);
            roboEyes9.setIdleMode(ON, 2, 1);
          }

          if(sad_stage == 1 && elapsed >= 5000){
            active_eye = 9;
            roboEyes9.setMood(TIRED);
            sad_stage = 2;
            roboEyes9.setAutoblinker(ON, 2, 1);
            //roboEyes9.setIdleMode(ON, 1, 1);
            roboEyes9.setCuriosity(true);
          }

          if(sad_stage == 2 && elapsed >= 11000){
            active_eye = 9;
            roboEyes9.setMood(TIRED);
            sad_stage = 3;
            roboEyes9.setAutoblinker(OFF);
            roboEyes9.setIdleMode(OFF);
            roboEyes9.setCuriosity(false);
            if(!is_moving_ud && count == 0){
              temp_id = move_ud(active_eye);
            }
            if(count >= 4){
              sad_stage = 4;
              is_moving_ud = false;
              count = 0;
            }
          }

          if(sad_stage == 3 && elapsed >= 15000){
            display.clearDisplay();
            active_eye = 10;
            roboEyes10.setMood(TIRED);
            sad_stage = 4;
            roboEyes10.setAutoblinker(ON, 2, 1);
            roboEyes10.setIdleMode(ON, 2, 1);
          }

          if(sad_stage == 4 && elapsed >= 18000){
            display.clearDisplay();
            active_eye = 10;
            roboEyes10.setMood(TIRED);
            sad_stage = 5;
            roboEyes10.setAutoblinker(ON, 1, 1);
            roboEyes10.setIdleMode(ON, 1, 1);
          }

          if(sad_stage == 5 && elapsed >= 21000){
            display.clearDisplay();
            active_eye = 10;
            roboEyes10.setMood(TIRED);
            sad_stage = 6;
            roboEyes10.setAutoblinker(OFF);
            roboEyes10.setIdleMode(ON, 0.5, 0.5);
          }

          if(sad_stage == 6 && elapsed >= 25000){
            display.clearDisplay();
            active_eye = 11;
            roboEyes11.setMood(TIRED);
            sad_stage = 7;
            roboEyes11.setAutoblinker(ON, 1, 1);
            roboEyes11.setPosition(S);
          }

          if(sad_stage == 7 && elapsed >= 29000){
            roboEyes11.setMood(TIRED);
            sad_stage = 8;
            roboEyes11.setAutoblinker(ON, 2, 1);
            roboEyes11.setIdleMode(ON, 2, 1);
          }

          if(sad_stage == 8 && elapsed >= 34000){
            roboEyes11.setMood(TIRED);
            sad_stage = 9;
            roboEyes11.setAutoblinker(ON, 2, 1);
            roboEyes11.setIdleMode(ON, 0.5, 0.5);
          }

          if(sad_stage == 9 && elapsed >= 38000){
            display.clearDisplay();
            active_eye = 10;
            sad();
          } 
        }

        if(is_chappy){
          unsigned elapsed = millis() - chappy_start_time;

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 500 && elapsed_ud < 1000 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(S);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(S);
              }
              else if(active_eye == 13){
                roboEyes13.setPosition(S);
              }
              else if(active_eye == 14){
                roboEyes14.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 1000){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(N);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(N);
              }
              else if(active_eye == 13){
                roboEyes13.setPosition(N);
              }
              else if(active_eye == 14){
                roboEyes14.setPosition(N);
              }
            }
          }

          if(chappy_stage == 0 && elapsed >= 3000){
            active_eye = 12;
            roboEyes12.setMood(HAPPY);
            roboEyes12.setAutoblinker(ON, 2, 1);
            roboEyes12.setIdleMode(ON, 2, 1);
            chappy_stage = 1;
          }

          if(chappy_stage == 1 && elapsed >= 6000){
            roboEyes12.setIdleMode(OFF);
            if(!is_moving_ld && count == 0){
              temp_id = move_ld(active_eye);
            }
            if(count >= 3){
              chappy_stage = 2;
              is_moving_ld = false;
              count = 0;
            }
          }

          if(chappy_stage == 2 && elapsed >= 9000){//1200*3 = 3600
            active_eye = 13;
            roboEyes13.setMood(HAPPY);
            roboEyes13.setAutoblinker(ON, 1, 1);
            roboEyes13.setIdleMode(OFF);
            chappy_stage = 3;
          }

          if(chappy_stage == 3 && elapsed >= 12000){
            active_eye = 13;
            roboEyes13.setMood(HAPPY);
            roboEyes13.setAutoblinker(ON, 1, 1);
            roboEyes13.setIdleMode(ON, 1, 1);
            chappy_stage = 4;
          }

          if(chappy_stage == 4 && elapsed >= 14000){
            active_eye = 12;
            roboEyes12.setMood(DEFAULT);
            roboEyes12.setAutoblinker(ON, 1, 0);
            roboEyes12.setIdleMode(OFF);
            chappy_stage = 5;
          }

          if(chappy_stage == 5 && elapsed >= 15000){
            active_eye = 13;
            roboEyes13.setMood(HAPPY);
            roboEyes13.setAutoblinker(ON, 1, 0);
            roboEyes13.setIdleMode(OFF);
            chappy_stage = 6;
          }

          if(chappy_stage == 6 && elapsed >= 16000){
            active_eye = 13;
            roboEyes13.setMood(DEFAULT);
            roboEyes13.setAutoblinker(ON, 1, 0);
            roboEyes13.setIdleMode(OFF);
            chappy_stage = 7;
          }

          if(chappy_stage == 7 && elapsed >= 17000){
            active_eye = 13;
            roboEyes13.setMood(HAPPY);
            roboEyes13.setAutoblinker(ON, 1, 0);
            roboEyes13.setIdleMode(OFF);
            chappy_stage = 8;
          }

          if(chappy_stage == 8 && elapsed >= 18000){
            active_eye = 14;
            roboEyes14.setMood(DEFAULT);
            roboEyes14.setAutoblinker(ON, 1, 0);
            roboEyes14.setIdleMode(OFF);
            chappy_stage = 9;
          }

          if(chappy_stage == 9 && elapsed >= 19000){
            active_eye = 14;
            roboEyes14.setMood(HAPPY);
            roboEyes14.setAutoblinker(ON, 1, 0);
            roboEyes14.setIdleMode(OFF);
            chappy_stage = 10;
          }

          if(chappy_stage == 10 && elapsed >= 20000){
            roboEyes14.setIdleMode(OFF);
            if(!is_moving_ld && count == 0){
              temp_id = move_ld(active_eye);
            }
            if(count >= 3){
              chappy_stage = 11;
              is_moving_ld = false;
              count = 0;
            }
          }

          if(chappy_stage == 11 && elapsed >= 23000){
            active_eye = 14;
            roboEyes14.setMood(HAPPY);
            roboEyes14.setAutoblinker(ON, 2, 1);
            roboEyes14.setIdleMode(ON,2,1);
            chappy_stage = 12;
          }

          if(chappy_stage == 12 && elapsed >= 26000){
            display.clearDisplay();
            active_eye = 12;
            happy_chill();
          }
        }

        if(is_ehappy){
          unsigned elapsed = millis() - ehappy_start_time;

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 250 && elapsed_ud < 500 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(S);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(S);
              }
              else if(active_eye == 15){
                roboEyes15.setPosition(S);
              }
              else if(active_eye == 16){
                roboEyes16.setPosition(S);
              }
              else if(active_eye == 17){
                roboEyes17.setPosition(S);
              }
              else if(active_eye == 18){
                roboEyes18.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 500){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(N);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(N);
              }
              else if(active_eye == 15){
                roboEyes15.setPosition(N);
              }
              else if(active_eye == 16){
                roboEyes16.setPosition(N);
              }
              else if(active_eye == 17){
                roboEyes17.setPosition(N);
              }
              else if(active_eye == 18){
                roboEyes18.setPosition(N);
              }
            }
          }

          if(ehappy_stage == 0 && elapsed >= 2000){
              active_eye = 15;
              roboEyes15.setMood(HAPPY);
              roboEyes15.setAutoblinker(ON, 1, 1);
              roboEyes15.setIdleMode(ON, 0.5,0.5);
              ehappy_stage = 1;
          }

          if(ehappy_stage == 1 && elapsed >= 4000){
              active_eye = 15;
              roboEyes15.setMood(HAPPY);
              roboEyes15.setAutoblinker(ON, 1, 1);
              roboEyes15.setIdleMode(ON, 1,1);
              ehappy_stage = 2;
          }

          if(ehappy_stage == 2 && elapsed >= 6000){
              active_eye = 15;
              roboEyes15.setMood(HAPPY);
              roboEyes15.setAutoblinker(ON, 1, 1);
              roboEyes15.setIdleMode(ON, 0.5,0.5);
              ehappy_stage = 3;
          }

          if(ehappy_stage == 3 && elapsed >= 8000){
              active_eye = 15;
              roboEyes15.setMood(HAPPY);
              roboEyes15.setAutoblinker(ON, 1, 1);
              roboEyes15.setIdleMode(ON, 1,1);
              ehappy_stage = 4;
          }

          if(ehappy_stage == 4 && elapsed >= 10000){
              active_eye = 16;
              roboEyes16.setMood(HAPPY);
              roboEyes16.setAutoblinker(ON, 1, 1);
              roboEyes16.setIdleMode(ON, 1, 1);
              ehappy_stage = 5;
          }

          if(ehappy_stage == 5 && elapsed >= 12000){
              roboEyes16.setIdleMode(OFF);
              if(!is_moving_ld && count == 0){
                temp_id = move_ld(active_eye);
              }
              if(count >= 6){
                ehappy_stage = 6;
                is_moving_ld = false;
                count = 0;
              }
          }

          if(ehappy_stage == 6 && elapsed >= 15000){
              active_eye = 16;
              roboEyes16.setMood(HAPPY);
              roboEyes16.setAutoblinker(ON, 2, 1);
              roboEyes16.setIdleMode(OFF);
              ehappy_stage = 7;
          }

          if(ehappy_stage == 7 && elapsed >= 17000){
              active_eye = 16;
              roboEyes16.setMood(HAPPY);
              roboEyes16.setAutoblinker(ON, 2, 1);
              roboEyes16.setCuriosity(true);
              //roboEyes16.setIdleMode(OFF);
              roboEyes16.setIdleMode(ON, 1, 1);
              ehappy_stage = 8;
          }

          if(ehappy_stage == 8 && elapsed >= 20000){
              active_eye = 16;
              roboEyes16.setMood(HAPPY);
              roboEyes16.setAutoblinker(ON, 2, 1);
              roboEyes16.setIdleMode(ON,0.5,0.5);
              ehappy_stage = 9;
          }

          if(ehappy_stage == 9 && elapsed >= 23000){
              active_eye = 17;
              roboEyes17.setMood(HAPPY);
              roboEyes17.setAutoblinker(ON, 1, 1);
              roboEyes17.setIdleMode(OFF);
              ehappy_stage = 10;
          }

          if(ehappy_stage == 10 && elapsed >= 24000){
              active_eye = 17;
              roboEyes17.setMood(HAPPY);
              roboEyes17.setAutoblinker(ON, 1, 1);
              roboEyes17.setCuriosity(true);
              //roboEyes17.setIdleMode(OFF);
              roboEyes17.setIdleMode(ON, 1, 1);
              ehappy_stage = 11;
          }

          if(ehappy_stage == 11 && elapsed >= 27000){
              active_eye = 17;
              roboEyes17.setMood(HAPPY);
              roboEyes17.setAutoblinker(ON, 1, 1);
              roboEyes17.setIdleMode(OFF);
              ehappy_stage = 12;
          }

          if(ehappy_stage == 12 && elapsed >= 28000){
              active_eye = 17;
              roboEyes17.setMood(HAPPY);
              roboEyes17.setAutoblinker(ON, 1, 1);
              roboEyes17.setIdleMode(OFF);
              ehappy_stage = 13;
          }

          if(ehappy_stage == 13 && elapsed >= 29000){
              active_eye = 16;
              roboEyes16.setMood(HAPPY);
              roboEyes16.setAutoblinker(ON, 1, 1);
              roboEyes16.setIdleMode(OFF);
              ehappy_stage = 14;
          }

          if(ehappy_stage == 14 && elapsed >= 30000){
              active_eye = 15;
              roboEyes15.setMood(HAPPY);
              roboEyes15.setAutoblinker(ON, 1, 1);
              roboEyes15.setIdleMode(OFF);
              ehappy_stage = 15;
          }

          if(ehappy_stage == 15 && elapsed >= 31000){
              active_eye = 18;
              roboEyes18.setMood(HAPPY);
              roboEyes18.setAutoblinker(ON, 1, 1);
              roboEyes18.setIdleMode(OFF);
              ehappy_stage = 16;
          }

          if(ehappy_stage == 16 && elapsed >= 33000){
              active_eye = 18;
              roboEyes18.setMood(HAPPY);
              roboEyes18.setAutoblinker(ON, 1, 1);
              roboEyes18.setIdleMode(ON,0.5, 0.5);
              ehappy_stage = 19;
          }

          if(ehappy_stage == 19 && elapsed >= 35000){
              active_eye = 15;
              display.clearDisplay();
              happy_energetic();
          }
        }

        if(is_sromantic){
          unsigned elapsed = millis() - sromantic_start_time;

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 500 && elapsed_ud < 1000 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(S);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(S);
              }
              else if(active_eye == 19){
                roboEyes19.setPosition(S);
              }
              else if(active_eye == 21){
                roboEyes21.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 1000){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(N);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(N);
              }
              else if(active_eye == 19){
                roboEyes19.setPosition(N);
              }
              else if(active_eye == 21){
                roboEyes21.setPosition(N);
              }
            }
          }

          if(sromantic_stage == 0 && elapsed >= 2000){
            active_eye = 19;
            roboEyes19.setMood(HAPPY);
            roboEyes19.setAutoblinker(ON, 1, 1);
            roboEyes19.setIdleMode(OFF);
            sromantic_stage = 1;
          }

          if(sromantic_stage == 1 && elapsed >= 4000){
            active_eye = 19;
            roboEyes19.setIdleMode(OFF);
            roboEyes19.setAutoblinker(ON,2,1);
              if(!is_moving_ld && count == 0){
                temp_id = move_ld(active_eye);
              }
              if(count >= 3){
                sromantic_stage = 2;
                is_moving_ld = false;
                count = 0;
              }
          }

          if(sromantic_stage == 2 && elapsed >= 7000){
            active_eye = 19;
            roboEyes19.setMood(HAPPY);
            roboEyes19.setAutoblinker(ON, 1, 1);
            roboEyes19.setIdleMode(ON,1,1);
            sromantic_stage = 3;
          }

          if(sromantic_stage == 3 && elapsed >= 10000){
            active_eye = 20;
            roboEyes20.setMood(TIRED);
            roboEyes20.setAutoblinker(ON,2,1);
            roboEyes20.setIdleMode(OFF);
            sromantic_stage = 4;
          }

          if(sromantic_stage == 4 && elapsed >= 13000){
            active_eye = 20;
            roboEyes20.setMood(TIRED);
            roboEyes20.setAutoblinker(ON,1,1);
            roboEyes20.setIdleMode(ON,1,1);
            sromantic_stage = 5;
          }

          if(sromantic_stage == 5 && elapsed >= 16000){
            active_eye = 21;
            roboEyes21.setMood(DEFAULT);
            roboEyes21.setAutoblinker(OFF);
            roboEyes21.setIdleMode(ON, 1, 1);
            sromantic_stage = 6;
          }

          if(sromantic_stage == 6 && elapsed >= 19000){
            active_eye = 21;
            roboEyes21.setIdleMode(OFF);
            roboEyes21.setAutoblinker(ON,2,1);
              if(!is_moving_ld && count == 0){
                temp_id = move_ld(active_eye);
              }
              if(count >= 3){
                sromantic_stage = 7;
                is_moving_ld = false;
                count = 0;
              }
            }

          if(sromantic_stage == 7 && elapsed >= 22000){
            active_eye = 19;
            roboEyes19.setMood(DEFAULT);
            roboEyes19.setAutoblinker(ON,1,1);
            roboEyes19.setIdleMode(ON, 1, 1);
            sromantic_stage = 8;
          }

          if(sromantic_stage == 8 && elapsed >= 24000){
            active_eye = 19;
            display.clearDisplay();
            romantic_slow();
          }
        }

        if(is_hromantic){
          unsigned elapsed = millis() - hromantic_start_time;

          if(is_moving_ud){
            unsigned long elapsed_ud = millis() - movement_ud_time;
            
            if(elapsed_ud >= 250 && elapsed_ud < 500 && !up){
              if(active_eye == 7){
                roboEyes7.setPosition(S);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(S);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(S);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(S);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(S);
              }
              else if(active_eye == 19){
                roboEyes19.setPosition(S);
              }
              else if(active_eye == 21){
                roboEyes21.setPosition(S);
              }
              else if(active_eye == 22){
                roboEyes22.setPosition(S);
              }
              else if(active_eye == 23){
                roboEyes23.setPosition(S);
              }
              else if(active_eye == 24){
                roboEyes24.setPosition(S);
              }
              else if(active_eye == 25){
                roboEyes25.setPosition(S);
              }
              up = true;
            }

            if(elapsed_ud >= 500){
              count++;
              movement_ud_time = millis();
              up = false;

              if(active_eye == 7){
                roboEyes7.setPosition(N);
              }
              else if(active_eye == 6){
                roboEyes6.setPosition(N);
              }
              else if(active_eye == 8){
                roboEyes8.setPosition(N);
              }
              else if(active_eye == 9){
                roboEyes9.setPosition(N);
              }
              else if(active_eye == 12){
                roboEyes12.setPosition(N);
              }
              else if(active_eye == 19){
                roboEyes19.setPosition(N);
              }
              else if(active_eye == 21){
                roboEyes21.setPosition(N);
              }
              else if(active_eye == 22){
                roboEyes22.setPosition(N);
              }
              else if(active_eye == 23){
                roboEyes23.setPosition(N);
              }
              else if(active_eye == 24){
                roboEyes24.setPosition(N);
              }
              else if(active_eye == 25){
                roboEyes25.setPosition(N);
              }
            }
          }

          if(hromantic_stage == 0 && elapsed >= 2000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(ON,1,1);
            roboEyes22.setIdleMode(ON, 0.5, 0.5);
            hromantic_stage = 1;
          }

          if(hromantic_stage == 1 && elapsed >= 4000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(ON,1,1);
            roboEyes22.setIdleMode(ON, 1, 1);
            hromantic_stage = 2;
          }

          if(hromantic_stage == 2 && elapsed >= 5000){
            active_eye = 22;
            roboEyes22.setIdleMode(OFF);
            roboEyes22.setAutoblinker(ON,2,1);
              if(!is_moving_ld && count == 0){
                temp_id = move_ld(active_eye);
              }
              if(count >= 6){
                hromantic_stage = 3;
                is_moving_ld = false;
                count = 0;
              }
          }

          if(hromantic_stage == 3 && elapsed >= 8000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(OFF);
            roboEyes22.setIdleMode(OFF);
            hromantic_stage = 4;
          }

          if(hromantic_stage == 4 && elapsed >= 9000){
            active_eye = 23;
            roboEyes23.setMood(TIRED);
            roboEyes23.setAutoblinker(OFF);
            roboEyes23.setIdleMode(OFF);
            hromantic_stage = 5;
          }

          if(hromantic_stage == 5 && elapsed >= 10000){
            active_eye = 23;
            roboEyes23.setMood(TIRED);
            roboEyes23.setAutoblinker(OFF);
            roboEyes23.setIdleMode(ON, 0.5,0.5);
            hromantic_stage = 6;
          }

          if(hromantic_stage == 6 && elapsed >= 13000){
            active_eye = 25;
            roboEyes25.setMood(HAPPY);
            roboEyes25.setAutoblinker(OFF);
            roboEyes25.setIdleMode(OFF);
            hromantic_stage = 7;
          }

          if(hromantic_stage == 7 && elapsed >= 14000){
            active_eye = 25;
            roboEyes25.setMood(HAPPY);
            roboEyes25.setAutoblinker(ON, 1, 1);
            roboEyes25.setIdleMode(ON, 1, 1);
            hromantic_stage = 8;
          }

          if(hromantic_stage == 8 && elapsed >= 16000){
            active_eye = 24;
            roboEyes24.setMood(ANGRY);
            roboEyes24.setAutoblinker(OFF);
            roboEyes24.setIdleMode(OFF);
            hromantic_stage = 9;
          }

          if(hromantic_stage == 9 && elapsed >= 17000){
            active_eye = 24;
            roboEyes24.setMood(ANGRY);
            roboEyes24.setAutoblinker(OFF);
            roboEyes24.setIdleMode(ON,1,1);
            hromantic_stage = 10;
          }

          if(hromantic_stage == 10 && elapsed >= 20000){
            active_eye = 25;
            roboEyes25.setMood(HAPPY);
            roboEyes25.setAutoblinker(OFF);
            roboEyes25.setIdleMode(ON,0.5,0.5);
            hromantic_stage = 11;
          }

          if(hromantic_stage == 11 && elapsed >= 24000){
            active_eye = 25;
            roboEyes25.setMood(HAPPY);
            roboEyes25.setAutoblinker(ON,1,1);
            roboEyes25.setIdleMode(ON,1,1);
            hromantic_stage = 12;
          }

          if(hromantic_stage == 12 && elapsed >= 27000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(ON,1,1);
            roboEyes22.setIdleMode(ON,1,1);
            hromantic_stage = 13;
          }

          if(hromantic_stage == 13 && elapsed >= 30000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(ON,1,1);
            roboEyes22.setIdleMode(ON,0.5,0.5);
            hromantic_stage = 14;
          }

          if(hromantic_stage == 14 && elapsed >= 33000){
            active_eye = 22;
            roboEyes22.setMood(HAPPY);
            roboEyes22.setAutoblinker(ON,2,1);
            roboEyes22.setIdleMode(OFF);
            hromantic_stage = 15;
          }

          if(hromantic_stage == 15 && elapsed >= 37000){
            active_eye = 22;
            display.clearDisplay();
            romantic_happy();
          }     
        }
      } // Close the emotion logic section
      
      // 2. CHECK FOR NEW COMMANDS
      // 'receivedEmotionID' is normally -1. 
      // If the Callback (Step 1) ran, it changed this to 1, 2, 3, etc.
      // BLE Handling
      if (receivedEmotionID != -1) {
        updateRobotMood(receivedEmotionID);    
        receivedEmotionID = -1; // Reset flag!
      }
  }
 // End of Loop



















