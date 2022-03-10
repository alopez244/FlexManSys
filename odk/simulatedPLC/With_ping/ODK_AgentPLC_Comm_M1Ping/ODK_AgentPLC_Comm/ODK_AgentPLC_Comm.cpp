#include "stdafx.h"
#include "ODK_Functions.h"
#include "ODK_StringHelper.h"
#include "tchar.h"
#include <iostream>
#include <sstream>
#include <iomanip>
#include <ctime>
#include <jni.h>
#include "userDefines.h"
#include <string>
#include "nlohmann\json.hpp"
using json = nlohmann::json;

// Include MQTT library
//#include "include/MQTTClient.h"

//MQTTClient client;

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

	time_t date = seconds;



	tm* ltm = localtime(&date);

	
	std::string value;
	value = std::to_string(1900 + ltm->tm_year); value += "/"; value += std::to_string(1 + ltm->tm_mon); value += "/"; value += std::to_string(ltm->tm_mday); value += " "; value += std::to_string(ltm->tm_hour); value += ":"; value += std::to_string(ltm->tm_min); value += ":"; value += std::to_string(ltm->tm_sec);
	
	//value = std::to_string(years); value += "/"; value += std::to_string(months); value += "/"; value += std::to_string(days_remainder); value += " "; value += std::to_string(hours_remainder); value += ":"; value += std::to_string(minutes_remainder); value += ":"; value += std::to_string(seconds_remainder);
	return value;
}
/*
void connectWithBroker() {
	//Aquí creamos la conexión con el broker MQTT
	char SERVER_ADDRESS[256] = "192.168.2.240:31883";
	char CLIENT_ID[256] = "odk_1";
	char TOPIC[256] = "1";

	MQTTClient_create(&client, SERVER_ADDRESS, CLIENT_ID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
	MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;

	conn_opts.username = "admin";
	conn_opts.password = "mosquittoGCIS";

	int rc;
	if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS) {
		//debugFile << "\n Failed to connect, return code %d\n";
	}
	//debugFile << "\n Connection successful!";

	//Aquí cierro el fichero de debugueo
	//debugFile.close();
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
*/
ODK_RESULT initAgent() {
	static bool uniqueStart = false;	//Variable to avoid multiple initializations
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
	return ODK_SUCCESS;
}

//Parses a String in JSON format and leave its data in a structure
ODK_RESULT SampleRead(/*OUT*/agent2plc& str_in,	/*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData, /*INOUT*/control_flags& flags) {

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
		flags.Control_Flag_Item_Completed = false;
		flags.Control_Flag_Service_Completed = false;
		str_in.Id_Machine_Reference = j["Id_Machine_Reference"].get<int>();
		str_in.Id_Order_Reference = j["Id_Order_Reference"].get<int>();
		str_in.Id_Batch_Reference = j["Id_Batch_Reference"].get<int>();
		str_in.Id_Ref_Subproduct_Type = j["Id_Ref_Subproduct_Type"].get<int>();
		str_in.Operation_Ref_Service_Type = j["Operation_Ref_Service_Type"].get<int>();

		str_in.Operation_No_of_Items = j["Operation_No_of_Items"].get<int>();

		ret = ODK_SUCCESS;
	}
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
// Inicio de MQTT se comenta
	// Nos conectamos al broker
	/*
	connectWithBroker();

	// Una vez añadidos todos los datos, los subiremos al broker via MQTT
	char* charPayload = (char*)totalPayload.c_str();
	char* charMachineId = (char*)machineId.c_str();

	publishData(charMachineId, charPayload);
	*/
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


ODK_RESULT returnstate(const ODK_BOOL& pedidoactivo, const ODK_BOOL& detectederror) {
	bool ask = (bool)env->CallStaticBooleanMethod(cls, JNI_askstate);
	if (ask == true) {
	if (pedidoactivo == true && detectederror == false) {
		jstring state = env->NewStringUTF("Working");
		env->CallStaticVoidMethod(cls, JNI_rcvstate, state);
	}
	else if (pedidoactivo == false && detectederror == false) {
		jstring state = env->NewStringUTF("Not working");
		env->CallStaticVoidMethod(cls, JNI_rcvstate, state);
	}
	else if (pedidoactivo == true && detectederror == true) {
		jstring state = env->NewStringUTF("Error while working");
		env->CallStaticVoidMethod(cls, JNI_rcvstate, state);
	}
	else if (pedidoactivo == false && detectederror == true) {
		jstring state = env->NewStringUTF("Error while not working");
		env->CallStaticVoidMethod(cls, JNI_rcvstate, state);
	}
	else {
		jstring state = env->NewStringUTF("Unknown");
		env->CallStaticVoidMethod(cls, JNI_rcvstate, state);
	}

}
	while (ask == true) {
		ask = (bool)env->CallStaticBooleanMethod(cls, JNI_askstate);
	}

	if (env->ExceptionOccurred())
	{
		env->ExceptionDescribe();
	}
	return ODK_SUCCESS;
}




bool JNIinit(void) {
	ODK_TRACE("-->JNIinit");
	//The javaVM parameters are assigned and initialized

	JavaVMOption options[1];
	JavaVMInitArgs vm_args;	

	typedef jint(JNICALL* pCreateJavaVM)(JavaVM**, void**, void*);
	HINSTANCE hInstance = LoadLibrary(L"C:\\Program Files (x86)\\Java\\jre1.8.0_271\\bin\\client\\jvm.dll");
	pCreateJavaVM CreateJavaVM = (pCreateJavaVM)GetProcAddress(hInstance, "JNI_CreateJavaVM");

	//options[0].optionString = "-Djava.class.path=C:\\Users\\aabadia004\\IdeaProjects\\FlexManSys\\classes;C:\\Users\\aabadia004\\Documents\\java\\libraries\\jade\\lib\\jade.jar;C:\\Users\\aabadia004\\Documents\\Java\\libraries\\gson\\gson-2.8.6.jar;C:\\Users\\aabadia004\\Documents\\Java\\libraries\\commons collections\\commons-collections4-4.4.jar";
	options[0].optionString = "-Djava.class.path=C:\\Users\\dgarcia129\\IdeaProjects\\FlexManSys\\classes;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\jade.jar;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\gson-2.8.6.jar;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\commons-collections4-4.4.jar";
	//options[0].optionString = "-Djava.class.path=C:\\FlexManSys\\classes;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\jade-4.3.jar;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\gson-2.8.6.jar;C:\\Users\\dgarcia129\\Documents\\Java\\Libraries\\commons-collections4-4.4.jar";
	memset(&vm_args, 0, sizeof(vm_args));
	vm_args.version = JNI_VERSION_1_6;
	vm_args.nOptions = 1;
	vm_args.options = options;
	vm_args.ignoreUnrecognized = JNI_TRUE;
	//long status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	long status = CreateJavaVM(&jvm, (void**)&env, &vm_args);
	if (status == JNI_ERR) {
		ODK_TRACE("--Error creating JavaVM");
		return false;
	}
	//The class that contains the necessary functions is searched for
	cls = env->FindClass("es/ehu/domain/manufacturing/agents/cognitive/ExternalJADEgw");
	if (cls == NULL) {
		ODK_TRACE("--Error finding the class");
		return false;
	}
	//The static agentInit method is searched inside the class
	JNI_init = env->GetStaticMethodID(cls, "agentInit", "(Ljava/lang/String;)V");		//javap -s -p <clase>
	if (JNI_init == 0) {
		ODK_TRACE("--Error finding method agentInit");
		return false;
	}
	//The static send method is searched inside the class
	JNI_send = env->GetStaticMethodID(cls, "send", "(Ljava/lang/String;)V");		//javap -s -p <clase>
	if (JNI_send == 0) {
		ODK_TRACE("--Error finding method send");
		return false;
	}
	//The recv send method is searched inside the class
	JNI_recv = env->GetStaticMethodID(cls, "recv", "()Ljava/lang/String;");		//javap -s -p <clase>
	if (JNI_recv == 0) {
		ODK_TRACE("--Error finding method recv");
		return false;
	}
	
	JNI_rcvstate = env->GetStaticMethodID(cls, "rcvstate", "(Ljava/lang/String;)V");		//javap -s -p <clase>
	if (JNI_rcvstate == 0) {
		ODK_TRACE("--Error finding method rcvstate");
		return false;
	}
	JNI_askstate = env->GetStaticMethodID(cls, "askstate", "()Z");		//javap -s -p <clase>
	if (JNI_askstate == 0) {
		ODK_TRACE("--Error finding method askstate");
		return false;
	}

	ODK_TRACE("<--JNIinit");
	return true;
}

