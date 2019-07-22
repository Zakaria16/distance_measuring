#define TRIG_PIN 8
#define ECHO_PIN 9

void setup() {
  pinMode(TRIG_PIN,OUTPUT);
  pinMode(ECHO_PIN,INPUT);

  Serial.begin(9600);

}

void loop() {
  double distance  = getDistance();
  if(distance <= 400){
  Serial.println(distance);
  }
  delay(400);

}


double getDistance(){
  long sigTime = 0;
  //to be sure the trig pin is low before we start
  digitalWrite(TRIG_PIN,LOW);
  delayMicroseconds(5);
  //make the trig pin high for 10 microseconds
  digitalWrite(TRIG_PIN,HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN,LOW);

  //read the pulse time in micrsoseconds
  sigTime = pulseIn(ECHO_PIN,HIGH);
  
  //compute the distance by dividing the time by 2 and 
  //multiplying the result by the velocity of sound in air in cm/us
  
  return sigTime*0.01715; //velocity of sound /2  cm per microseconds
}
