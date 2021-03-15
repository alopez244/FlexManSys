#include "stdafx.h"
#include "ODK_Functions.h"
#include "ODK_StringHelper.h"
#include "tchar.h"

#include <jni.h>
#include "userDefines.h"
#include <string>
#include "nlohmann\json.hpp"
using json = nlohmann::json;


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

	/*Cuarto, pasamos a días y guardamos el resto*/
	unsigned long long days = hours / 24;
	int hours_remainder = hours % 24;

	/*Quinto, calculamos el año primero para poder calcular después el mes con mayor precisión*/
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

	/*Sexto, comprobamos si el año es bisiesto, y en funcion de eso se fijan el día y el mes*/
	int months = 0;
	if ((years % 4 == 0 && years % 100 != 0) || years % 400 == 0)
	{
		/*Es año bisiesto*/
		if (days_remainder <= 31)

		{
			months = 1;
			/*El mes es enero (1) y el día es lo que valga days_remainder*/
		}
		else if (days_remainder >= 32 && days_remainder <= 60)
		{
			months = 2;
			days_remainder = days_remainder - 31; /*El día es lo que vale days_remainder menos los días de enero*/
		}
		else if (days_remainder >= 61 && days_remainder <= 91)
		{
			months = 3;
			days_remainder = days_remainder - 60; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 92 && days_remainder <= 121)
		{
			months = 4;
			days_remainder = days_remainder - 91; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 122 && days_remainder <= 152)
		{
			months = 5;
			days_remainder = days_remainder - 121; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 153 && days_remainder <= 182)
		{
			months = 6;
			days_remainder = days_remainder - 152; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 183 && days_remainder <= 213)
		{
			months = 7;
			days_remainder = days_remainder - 182; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 214 && days_remainder <= 244)
		{
			months = 8;
			days_remainder = days_remainder - 213; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 245 && days_remainder <= 274)
		{
			months = 9;
			days_remainder = days_remainder - 244; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 275 && days_remainder <= 305)
		{
			months = 10;
			days_remainder = days_remainder - 274; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 306 && days_remainder <= 335)
		{
			months = 11;
			days_remainder = days_remainder - 305; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 335 && days_remainder <= 366)
		{
			months = 12;
			days_remainder = days_remainder - 335; /*El día es lo que vale days_remainder menos el acumulado*/
		}
	}
	else
	{
		/*No bisiesto*/
		if (days_remainder <= 31)
		{
			months = 1;
			/*El mes es enero (1) y el día es lo que valga days_remainder*/
		}
		else if (days_remainder >= 32 && days_remainder <= 59)
		{
			months = 2;
			days_remainder = days_remainder - 31; /*El día es lo que vale days_remainder menos los días de enero*/
		}
		else if (days_remainder >= 60 && days_remainder <= 90)
		{
			months = 3;
			days_remainder = days_remainder - 59; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 91 && days_remainder <= 120)
		{
			months = 4;
			days_remainder = days_remainder - 90; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 121 && days_remainder <= 151)
		{
			months = 5;
			days_remainder = days_remainder - 120; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 152 && days_remainder <= 181)
		{
			months = 6;
			days_remainder = days_remainder - 151; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 182 && days_remainder <= 212)
		{
			months = 7;
			days_remainder = days_remainder - 181; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 213 && days_remainder <= 243)
		{
			months = 8;
			days_remainder = days_remainder - 212; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 244 && days_remainder <= 273)
		{
			months = 9;
			days_remainder = days_remainder - 243; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 274 && days_remainder <= 304)
		{
			months = 10;
			days_remainder = days_remainder - 273; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 305 && days_remainder <= 334)
		{
			months = 11;
			days_remainder = days_remainder - 304; /*El día es lo que vale days_remainder menos el acumulado*/
		}
		else if (days_remainder >= 334 && days_remainder <= 365)
		{
			months = 12;
			days_remainder = days_remainder - 334; /*El día es lo que vale days_remainder menos el acumulado*/
		}
	}

	std::string value;
	value = std::to_string(years); value += "/"; value += std::to_string(months); value += "/"; value += std::to_string(days_remainder); value += " "; value += std::to_string(hours_remainder); value += ":"; value += std::to_string(minutes_remainder); value += ":"; value += std::to_string(seconds_remainder);
	return value;
}

ODK_RESULT initAgent() {
	static bool uniqueStart = false;	//Variable to avoid multiple initializations
	if (!uniqueStart) {				//It only happens on the first run
		ODK_TRACE("->ODK initAgent");
		uniqueStart = true;			//Update the flag to prevent it from being run again
		if (!JNIinit())	return ODK_USER_ERROR_BASE;
		ODK_TRACE("calling func. Init");
		jstring machineID = env->NewStringUTF("1");	//Se inicializa como agente gateway para la maquina 1
		env->CallStaticIntMethod(cls, JNI_init, machineID);	//Executes the init method using JNI
		env->CallStaticObjectMethod(cls, JNI_recv);		//Executes de recv method to initialize the gateway agent in JADE´s GUI
		ODK_TRACE("<-ODK initAgent");
	}	
	return ODK_SUCCESS;
}

//Parses a String in JSON format and leave its data in a structure
ODK_RESULT recvAgent(/*OUT*/control_flags& str1, /*OUT*/agent2plc& str2,	/*OUT*/ODK_BOOL& tRecv, /*OUT*/ODK_BOOL& tData) {
	ODK_TRACE("->ODK recvAgent");
	char msgInCa[254] = { 0 };		//An array is created to reserve that memory
	const char* msgInC = msgInCa;	//Pointer to array of characters is created
	//Executes the recv method through JNI and receives a jstring with the message to read
	jstring s = (jstring)env->CallStaticObjectMethod(cls, JNI_recv);
	msgInC = env->GetStringUTFChars(s, 0);					//Type conversion jstring->char*
	//env->ReleaseStringUTFChars(s, msgInC);		//The conversion space is freed	

	sprintf(traza, "msg(C): %s",msgInC);	//Se forma el mensaje de la traza
	ODK_TRACE(traza);

	uint16_t ret;							//Variable to store the result of the function
	tRecv = false;
	tData = false;
	ODK_TRACE("->Parsing JSON to structure");
	json j = json::parse(msgInC);			//The String is parsed and converted to a JSON object
	ODK_TRACE("->Estructure obtained");
	if (j.contains("Received")) {			//It is checked if it is a receipt confirmation
		ODK_TRACE("--Received type Message");
		if (j["Received"] == true) {
			tRecv = true;
			ODK_TRACE("--Message received at Agent OK");
			ret = ODK_SUCCESS; 
		}
	}
	else if (j.contains("Control_Flag_New_Service")) {	//At least the first field is checked
		ODK_TRACE("Receiving new operation");
		tData = true;
		//The received parameters are copied to the output structure		
		str1.Control_Flag_New_Service =		j["Control_Flag_New_Service"].get<bool>();
		str2.Id_Machine_Reference  =		j["Id_Machine_Reference"].get<int>();
		str2.Id_Order_Reference =			j["Id_Order_Reference"].get<int>();
		str2.Id_Batch_Reference =			j["Id_Batch_Reference"].get<int>();
		str2.Id_Ref_Subproduct_Type =		j["Id_Ref_Subproduct_Type"].get<int>();
		str2.Operation_Ref_Service_Type =	j["Operation_Ref_Service_Type"].get<int>();
		str2.Operation_No_of_Items =		j["Operation_No_of_Items"].get<int>();
		
		ret = ODK_SUCCESS;
	}
	else {
		ODK_TRACE("->Waiting for a new message");
		ret = ODK_SUCCESS;
	}
	ODK_TRACE("<-ODK recvAgent");
	return ret;
}

//From a data structure a String is generated in JSON format
ODK_RESULT sendAgent(/*IN*/const control_flags& str1, /*IN*/const plc2agent& str2) {
	ODK_TRACE("->ODK sendAgent");
	json j;	//JSON Object
	//Each element of the structure is copied into a Json object
	j["Control_Flag_Item_Completed"] =		(bool)str1.Control_Flag_Item_Completed;
	j["Control_Flag_Service_Completed"] =	(bool)str1.Control_Flag_Service_Completed;
	j["Id_Machine_Reference"] =				(uint32_t)str2.Id_Machine_Reference;
	j["Id_Order_Reference"] =				(uint32_t)str2.Id_Order_Reference;
	j["Id_Batch_Reference"] =				(uint32_t)str2.Id_Batch_Reference;
	j["Id_Ref_Subproduct_Type"] =			(uint32_t)str2.Id_Ref_Subproduct_Type;
	j["Id_Ref_Service_Type"] =				(uint32_t)str2.Id_Ref_Service_Type;
	j["Id_Item_Number"] =					(uint8_t)str2.Id_Item_Number;

	unsigned long long Data_Initial_Time_Stamp_T = str2.Data_Initial_Time_Stamp;
	unsigned long long Data_Final_Time_Stamp_T = str2.Data_Final_Time_Stamp;
	unsigned long long Data_Service_Time_Stamp_T = str2.Data_Service_Time_Stamp;
	
	j["Data_Initial_Time_Stamp"] =			transformTimeStamp(Data_Initial_Time_Stamp_T);
	j["Data_Final_Time_Stamp"] =			transformTimeStamp(Data_Final_Time_Stamp_T);
	j["Data_Service_Time_Stamp"] =			transformTimeStamp(Data_Service_Time_Stamp_T);

	std::string msgJson = j.dump(); //json serialized in c++ string data type
	jstring msg = env->NewStringUTF(msgJson.c_str());	//Type conversion using UTF-8 string -> jstring
	env->CallStaticIntMethod(cls, JNI_send, msg);	//The send function is executed through JNI
	ODK_TRACE("<-sending finished");

	return ODK_SUCCESS;
}

//Generates a String in JSON format with the message receipt confirmation
ODK_RESULT sendConf() {
	ODK_TRACE("->ODK Sending confirmation");
	json j;							//JSON Object
	j["Received"] = true;			//The "Received" key is added to the object
	char JsonCa[256 + 1] = { 0 };		//An array is generated to reserve that memory
	char* JsonC = JsonCa;			//A pointer is created that points to the created array
	sprintf(JsonC, "%s", (j.dump()).c_str());	//Serialized->String->const char*
	jstring msg = env->NewStringUTF(JsonC);	//Type conversion char* -> jstring
	env->CallStaticIntMethod(cls, JNI_send, msg);	//The send function is executed through JNI
	ODK_TRACE("<-ODK Confirmation sended");
	return ODK_SUCCESS;
}


bool JNIinit(void) {
	ODK_TRACE("-->JNIinit");
	//The javaVM parameters are assigned and initialized

	JavaVMOption options[1];
	JavaVMInitArgs vm_args;	
	options[0].optionString = "-Djava.class.path=C:\\Users\\aabadia004\\IdeaProjects\\FlexManSys\\classes;C:\\Users\\aabadia004\\Documents\\java\\libraries\\jade\\lib\\jade.jar;C:\\Users\\aabadia004\\Documents\\Java\\libraries\\gson\\gson-2.8.6.jar;C:\\Users\\aabadia004\\Documents\\Java\\libraries\\commons collections\\commons-collections4-4.4.jar";
	memset(&vm_args, 0, sizeof(vm_args));
	vm_args.version = JNI_VERSION_1_6;
	vm_args.nOptions = 1;
	vm_args.options = options;
	long status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
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
	ODK_TRACE("<--JNIinit");
	return true;
}

