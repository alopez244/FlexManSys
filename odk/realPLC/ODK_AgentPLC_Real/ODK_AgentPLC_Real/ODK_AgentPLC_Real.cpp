#include "stdafx.h"
#include "ODK_Functions.h"
#include "ODK_StringHelper.h"
#include "tchar.h"

#include <jni.h>
#include "userDefines.h"
#include <string>
#include <iostream>
#include <fstream> 
#include "nlohmann\json.hpp"
using json = nlohmann::json;

// Include MQTT library
#include "include/MQTTClient.h"

MQTTClient client;


/*
* OnLoad() is invoked after the application binary was loaded.
* @return ODK_SUCCESS       notify, that no error occurs
*                            - OnRun() will be invoked automatically
*         any other value   notify, that an error occurs (user defined)
*                            - OnUnload() will be invoked automatically
*                            - ODK application will be unloaded automatically
*/
EXPORT_API ODK_RESULT OnLoad(void)
{
	// place your code here
	return ODK_SUCCESS;
}

/*
* OnUnload() is invoked before the application binary is unloaded or when
*            ODK-Host terminates.
* @return ODK_SUCCESS       notify, that no error occurs
*         any other value   notify, that an error occurs (user defined)
*                            - ODK application will be unloaded anyway
*/
EXPORT_API ODK_RESULT OnUnload(void)
{
	// place your code here
	return ODK_SUCCESS;
}

/*
* OnRun() is invoked when the CPU transitions to the RUN state and
*         after OnLoad().
* @return Does not affect the load operation or state transition.
*/
EXPORT_API ODK_RESULT OnRun(void)
{
	// place your code here
	return ODK_SUCCESS;
}

/*
* OnStop() is invoked when the CPU transitions to the STOP/SHUTDOWN state
*          or before unloading the ODK application.
* @return Does not affect the load operation or state transition.
*/
EXPORT_API ODK_RESULT OnStop(void)
{
	// place your code here
	return ODK_SUCCESS;
}


std::string transformTimeStamp(unsigned long long TimeStamp)
{

	/*Primero pasamos de nanosegundos a segundos*/
	unsigned long long seconds = TimeStamp / 1000000000;

	/*Segundo, pasamos a minutos y guardamos el resto*/
	unsigned long long minutes = seconds / 60;
	int seconds_remainder = seconds % 60;

	/*Tercero, pasamos a horas y guardamos el resto*/
	unsigned long long hours = minutes / 60;
	int minutes_remainder = minutes % 60;

	/*Cuarto, pasamos a d�as y guardamos el resto*/
	unsigned long long days = hours / 24;
	int hours_remainder = hours % 24;

	/*Quinto, calculamos el a�o primero para poder calcular despu�s el mes con mayor precisi�n*/
	int years = (int)(1970 + ((days - ((days / 365) / 4)) / 365));
	int days_remainder;

	if (((days / 365) % 4) == 3 && (days % 365) >= ((days / 365) / 4))
	{
		days_remainder = ((days - ((days / 365) / 4)) % 365);

		if (days_remainder == 0)
		{
			years = years - 1;
			days_remainder = 366;
		}
	}
	else
	{
		days_remainder = 1 + ((days - ((days / 365) / 4)) % 365);
	}

	/*Sexto, comprobamos si el a�o es bisiesto, y en funcion de eso se fijan el d�a y el mes*/
	int months = 0;
	if ((years % 4 == 0 && years % 100 != 0) || years % 400 == 0)
	{
		/*Es a�o bisiesto*/
		if (days_remainder <= 31)

		{
			months = 1;
			/*El mes es enero (1) y el d�a es lo que valga days_remainder*/
		}
		else if (days_remainder >= 32 && days_remainder <= 60)
		{
			months = 2;
			days_remainder = days_remainder - 31; /*El d�a es lo que vale days_remainder menos los d�as de enero*/
		}
		else if (days_remainder >= 61 && days_remainder <= 91)
		{
			months = 3;
			days_remainder = days_remainder - 60; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 92 && days_remainder <= 121)
		{
			months = 4;
			days_remainder = days_remainder - 91; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 122 && days_remainder <= 152)
		{
			months = 5;
			days_remainder = days_remainder - 121; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 153 && days_remainder <= 182)
		{
			months = 6;
			days_remainder = days_remainder - 152; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 183 && days_remainder <= 213)
		{
			months = 7;
			days_remainder = days_remainder - 182; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 214 && days_remainder <= 244)
		{
			months = 8;
			days_remainder = days_remainder - 213; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 245 && days_remainder <= 274)
		{
			months = 9;
			days_remainder = days_remainder - 244; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 275 && days_remainder <= 305)
		{
			months = 10;
			days_remainder = days_remainder - 274; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 306 && days_remainder <= 335)
		{
			months = 11;
			days_remainder = days_remainder - 305; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 335 && days_remainder <= 366)
		{
			months = 12;
			days_remainder = days_remainder - 335; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
	}
	else
	{
		/*No bisiesto*/
		if (days_remainder <= 31)
		{
			months = 1;
			/*El mes es enero (1) y el d�a es lo que valga days_remainder*/
		}
		else if (days_remainder >= 32 && days_remainder <= 59)
		{
			months = 2;
			days_remainder = days_remainder - 31; /*El d�a es lo que vale days_remainder menos los d�as de enero*/
		}
		else if (days_remainder >= 60 && days_remainder <= 90)
		{
			months = 3;
			days_remainder = days_remainder - 59; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 91 && days_remainder <= 120)
		{
			months = 4;
			days_remainder = days_remainder - 90; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 121 && days_remainder <= 151)
		{
			months = 5;
			days_remainder = days_remainder - 120; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 152 && days_remainder <= 181)
		{
			months = 6;
			days_remainder = days_remainder - 151; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 182 && days_remainder <= 212)
		{
			months = 7;
			days_remainder = days_remainder - 181; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 213 && days_remainder <= 243)
		{
			months = 8;
			days_remainder = days_remainder - 212; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 244 && days_remainder <= 273)
		{
			months = 9;
			days_remainder = days_remainder - 243; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 274 && days_remainder <= 304)
		{
			months = 10;
			days_remainder = days_remainder - 273; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 305 && days_remainder <= 334)
		{
			months = 11;
			days_remainder = days_remainder - 304; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 334 && days_remainder <= 365)
		{
			months = 12;
			days_remainder = days_remainder - 334; /*El d�a es lo que vale days_remainder menos el acumulado*/
		}
	}

	std::string value;
	value = std::to_string(years); value += "/"; value += std::to_string(months); value += "/"; value += std::to_string(days_remainder); value += " "; value += std::to_string(hours_remainder); value += ":"; value += std::to_string(minutes_remainder); value += ":"; value += std::to_string(seconds_remainder);
	return value;
}
bool connectWithBroker(std::string Id_Order_Reference, std::string Id_Batch_Reference, std::string Id_Item_Number) {

	//Aqu� creamos la conexi�n con el broker MQTT//
	std::ofstream debugFile;
	debugFile.open("C:\\Users\\Operator2\\Documents\\debugFile.txt", std::ios_base::app);
	char SERVER_ADDRESS[256] = "192.168.2.240:1883";
	char CLIENT_ID[256] = "odk_1";
	std::string topicString;
	topicString = Id_Order_Reference + "/" + Id_Batch_Reference + "/" + Id_Item_Number;
	char* aux = (char*)topicString.c_str();
	char TOPIC[256] = {aux[256]};

	MQTTClient_create(&client, SERVER_ADDRESS, CLIENT_ID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
	MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;

	conn_opts.username = "admin";
	conn_opts.password = "mosquittoGCIS";

	int rc;
	if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS) {
		debugFile << "\n Failed to connect, return code %d\n";
		return false;
	}
	debugFile << "\n\r Connection successful!";

	//Aqu� cierro el fichero de debugueo//
	debugFile.close();

	return true;
}

void publishData(char* TOPIC, char* payload) {
	//char TOPIC[256] = topic;

	MQTTClient_message pubmsg = MQTTClient_message_initializer;
	pubmsg.payload = payload;
	pubmsg.payloadlen = strlen(payload);
	pubmsg.qos = 0;
	pubmsg.retained = 0;
	MQTTClient_deliveryToken token;
	MQTTClient_publishMessage(client, TOPIC, &pubmsg, &token);
	MQTTClient_waitForCompletion(client, token, 1000L);

}

ODK_RESULT initAgent() {
	static bool uniqueStart = false;	//Variable to avoid multiple initializations
	std::ofstream debugFile;
	debugFile.open("C:\\Users\\Operator2\\Documents\\debugFile.txt", std::ios_base::app);
	if (!uniqueStart) {				//It only happens on the first run
		uniqueStart = true;			//Update the flag to prevent it from being run again
		if (!JNIinit())	return ODK_USER_ERROR_BASE;
		jstring machineID = env->NewStringUTF("1");	//Se inicializa como agente gateway para la maquina 1
		env->CallStaticVoidMethod(cls, JNI_init, machineID);	//Executes the init method using JNI
		if (env->ExceptionOccurred())
		{
			env->ExceptionDescribe();
		}
	}
	debugFile.close();
	return ODK_SUCCESS;
}

//Parses a String in JSON format and leave its data in a structure
ODK_RESULT SampleRead(/*OUT*/agent2plc& str_in,	/*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData, /*INOUT*/control_flags& flags) {
	std::ofstream debugFile;
	debugFile.open("C:\\Users\\Operator2\\Documents\\debugFile.txt", std::ios_base::app);

	char msgInCa[254] = { 0 };		//An array is created to reserve that memory
	const char* msgInC = msgInCa;	//Pointer to array of characters is created
									//Executes the recv method through JNI and receives a jstring with the message to read
	jstring s = (jstring)env->CallStaticObjectMethod(cls, JNI_recv);
	if (env->ExceptionOccurred())
	{
		env->ExceptionDescribe();
	}
	msgInC = env->GetStringUTFChars(s, 0);					//Type conversion jstring->char*

	uint16_t ret;							//Variable to store the result of the function
	tRecv = false;
	tData = false;
	json j = json::parse(msgInC);			//The String is parsed and converted to a JSON object
	if (j.contains("Received")) {			//It is checked if it is a receipt confirmation
		if (j["Received"] == true) {
			tRecv = true;
			ret = ODK_SUCCESS;
		}
	}
	else if (j.contains("Control_Flag_New_Service")) {	//At least the first field is checked
		tData = true;
		//The received parameters are copied to the output structure		
		flags.Control_Flag_New_Service = j["Control_Flag_New_Service"].get<bool>();
		str_in.Id_Machine_Reference = j["Id_Machine_Reference"].get<int>();
		str_in.Id_Order_Reference = j["Id_Order_Reference"].get<int>();
		str_in.Id_Batch_Reference = j["Id_Batch_Reference"].get<int>();
		str_in.Id_Ref_Subproduct_Type = j["Id_Ref_Subproduct_Type"].get<int>();
		str_in.Operation_Ref_Service_Type = j["Operation_Ref_Service_Type"].get<int>();

		str_in.Operation_No_of_Items = j["Operation_No_of_Items"].get<int>();

		ret = ODK_SUCCESS;
	}
	debugFile.close();
	return ret;
}

//From a data structure a String is generated in JSON format
ODK_RESULT SampleWrite(/*IN*/const plc2agent& str_out, /*INOUT*/ control_flags& flags) {
	json j;	//JSON Object
			//Each element of the structure is copied into a Json object
	j["Control_Flag_Item_Completed"] = (bool)flags.Control_Flag_Item_Completed;
	j["Control_Flag_Service_Completed"] = (bool)flags.Control_Flag_Service_Completed;
	j["Id_Machine_Reference"] = (uint32_t)str_out.Id_Machine_Reference;
	j["Id_Order_Reference"] = (uint32_t)str_out.Id_Order_Reference;
	j["Id_Batch_Reference"] = (uint32_t)str_out.Id_Batch_Reference;
	j["Id_Ref_Subproduct_Type"] = (uint32_t)str_out.Id_Ref_Subproduct_Type;
	j["Id_Ref_Service_Type"] = (uint32_t)str_out.Id_Ref_Service_Type;
	j["Id_Item_Number"] = (uint8_t)str_out.Id_Item_Number;

	unsigned long long Data_Initial_Time_Stamp_T = str_out.Data_Initial_Time_Stamp;
	unsigned long long Data_Final_Time_Stamp_T = str_out.Data_Final_Time_Stamp;
	unsigned long long Data_Service_Time_Stamp_T = str_out.Data_Service_Time_Stamp;

	j["Data_Initial_Time_Stamp"] = transformTimeStamp(Data_Initial_Time_Stamp_T);
	j["Data_Final_Time_Stamp"] = transformTimeStamp(Data_Final_Time_Stamp_T);
	j["Data_Service_Time_Stamp"] = transformTimeStamp(Data_Service_Time_Stamp_T);

	// Nos conectamos al broker

	/*bool connected = connectWithBroker(std::to_string(str_out.Id_Order_Reference), std::to_string(str_out.Id_Batch_Reference), std::to_string(str_out.Id_Item_Number));

	if (connected == true) {
		//Preparar formato para broker MQTT
		std::string machineId;
		std::string totalPayload;


		machineId = std::to_string(str_out.Id_Machine_Reference);
		totalPayload = "Id_Machine_Reference=" + std::to_string(str_out.Id_Machine_Reference);
		totalPayload = totalPayload + "&Id_Order_Reference=" + std::to_string(str_out.Id_Order_Reference);
		totalPayload = totalPayload + "&Id_Batch_Reference=" + std::to_string(str_out.Id_Batch_Reference);
		totalPayload = totalPayload + "&Id_Ref_Subproduct_Type=" + std::to_string(str_out.Id_Ref_Subproduct_Type);
		totalPayload = totalPayload + "&Id_Ref_Service_Type=" + std::to_string(str_out.Id_Ref_Service_Type);
		totalPayload = totalPayload + "&Id_Item_Number=" + std::to_string(str_out.Id_Item_Number);
		totalPayload = totalPayload + "&Data_Initial_Time_Stamp=" + transformTimeStamp(Data_Initial_Time_Stamp_T);
		totalPayload = totalPayload + "&Data_Final_Time_Stamp=" + transformTimeStamp(Data_Final_Time_Stamp_T);

		if (flags.Control_Flag_Service_Completed == true) {
			totalPayload = totalPayload + "&Data_Service_Time_Stamp=" + transformTimeStamp(Data_Service_Time_Stamp_T);
		}

		// Una vez a�adidos todos los datos, los subiremos al broker via MQTT
		char* charPayload = (char*)totalPayload.c_str();
		char* charMachineId = (char*)machineId.c_str();

		publishData(charMachineId, charPayload);
	}*/	

	std::string msgJson = j.dump(); //json serialized in c++ string data type
	jstring msg = env->NewStringUTF(msgJson.c_str());	//Type conversion using UTF-8 string -> jstring
	env->CallStaticVoidMethod(cls, JNI_send, msg);	//The send function is executed through JNI
	if (env->ExceptionOccurred())
	{
		env->ExceptionDescribe();
	}

	return ODK_SUCCESS;
}

//Generates a String in JSON format with the message receipt confirmation
ODK_RESULT sendConf() {
	json j;							//JSON Object
	j["Received"] = true;			//The "Received" key is added to the object
	char JsonCa[256 + 1] = { 0 };		//An array is generated to reserve that memory
	char* JsonC = JsonCa;			//A pointer is created that points to the created array
	sprintf(JsonC, "%s", (j.dump()).c_str());	//Serialized->String->const char*
	jstring msg = env->NewStringUTF(JsonC);	//Type conversion char* -> jstring
	env->CallStaticVoidMethod(cls, JNI_send, msg);	//The send function is executed through JNI
	if (env->ExceptionOccurred())
	{
		env->ExceptionDescribe();
	}
	return ODK_SUCCESS;
}


bool JNIinit(void) {

	//The javaVM parameters are assigned and initialized

	JavaVMOption options[1];
	JavaVMInitArgs vm_args;
	options[0].optionString = "-Djava.class.path=C:\\Users\\Operator2\\Documents\\FlexManSys\\classes;C:\\Users\\Operator2\\Documents\\java\\libraries\\jade\\lib\\jade.jar;C:\\Users\\Operator2\\Documents\\java\\libraries\\gson\\gson-2.8.6.jar;C:\\Users\\Operator2\\Documents\\java\\libraries\\commons collections\\commons-collections4-4.4.jar";
	memset(&vm_args, 0, sizeof(vm_args));
	vm_args.version = JNI_VERSION_1_6;
	vm_args.nOptions = 1;
	vm_args.options = options;
	std::ofstream debugFile;
	debugFile.open("C:\\Users\\Operator2\\Documents\\debugFile.txt", std::ios_base::app);
	long status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	if (status == JNI_ERR) {
		debugFile << "\r\n JNIinit status error.";
		return false;
	}
	//The class that contains the necessary functions is searched for
	cls = env->FindClass("es/ehu/domain/manufacturing/agents/cognitive/ExternalJADEgw");
	if (env->ExceptionCheck()) {
		debugFile << "\r\n JNIinit findClass error.";
		return false;
	}
	//The static agentInit method is searched inside the class
	JNI_init = env->GetStaticMethodID(cls, "agentInit", "(Ljava/lang/String;)V");		//javap -s -p <clase>
	if (JNI_init == 0) {
		debugFile << "\r\n JNIinit agentInit error.";
		return false;
	}
	//The static send method is searched inside the class
	JNI_send = env->GetStaticMethodID(cls, "send", "(Ljava/lang/String;)V");		//javap -s -p <clase>
	if (JNI_send == 0) {
		//debugFile << "\r\n JNIinit send error.";
		return false;
	}
	//The recv send method is searched inside the class
	JNI_recv = env->GetStaticMethodID(cls, "recv", "()Ljava/lang/String;");		//javap -s -p <clase>
	if (JNI_recv == 0) {
		//debugFile << "\r\n JNIinit recv error.";
		return false;
	}
	debugFile.close();
	return true;
}

