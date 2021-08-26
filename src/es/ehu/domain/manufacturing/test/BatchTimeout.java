
package es.ehu.domain.manufacturing.test;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDateTime;
import jade.wrapper.AgentController;

public class BatchTimeout extends Agent{
    private ACLMessage msgfrombatch;
    private String rawfinishtime=null;
    private MessageTemplate templatebatch;
    private ArrayList<String> items_finish_times=new ArrayList<String>();
    private Date now=null;
    private String batchreference;
    private ACLMessage msg=null;
    private Date expected_finish_date=null;
    private Date date_when_delay_was_asked=null;
    private boolean delay_already_asked=false;
    private boolean delay_already_incremented=false;
    private long delaynum=0;
    private boolean takedown_flag=false;


    protected void setup(){

        templatebatch=MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchOntology(getLocalName()));                             //se pidem los finish
        msgfrombatch=blockingReceive(templatebatch);
        rawfinishtime=msgfrombatch.getContent();
        items_finish_times=take_finish_times(rawfinishtime);
        batchreference=get_batch_ID(rawfinishtime);
        int j=0;
        while(!takedown_flag) {
            if (j < items_finish_times.size()) {        //se comprueba item a item si ha habido timeout

                now = getactualtime();

                try {
                    if(!delay_already_incremented) {
                        expected_finish_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(j));
                    }
                    while (now.before(expected_finish_date)&&msg==null) {//mientras que no se exceda el tiempo definido que compruebe que no haya habido timeout
//                        Thread.sleep(100);
                        now = getactualtime();
                        msg = receive();
                        if(msg!=null) {
                            if (msg.getOntology().equals("timeout_reset")) {
                                j++;
                                delay_already_incremented = false;
                                System.out.println("Item feedback received. Updating finish time");
                            }
                        }
                    }
                    now = getactualtime();
                    if (now.after(expected_finish_date)){
                    if(delay_already_asked) {                  //Si ya se ha pedido el delay, actualiza el finish time del item seleccionado
                        if (j == 0 && !delay_already_incremented) {
                            long startime = date_when_delay_was_asked.getTime() - (delaynum);    //Para calcular el tiempo de operación se necesita calcular el start time de la primera operacion
//                            Date d = new Date(startime); //descomentar para debug
                            LocalDateTime new_expected_finish_time = convertToLocalDateTimeViaSqlTimestamp(now);
                            new_expected_finish_time = new_expected_finish_time.plusSeconds(((expected_finish_date.getTime() - startime) / 1000)+1);
                            expected_finish_date = convertToDateViaSqlTimestamp(new_expected_finish_time);   //el finish time se calcula segun el tiempo de operacion y la fecha actual
                            System.out.println("Finish time of operation incremented. Caused by delay on machine plan startup");
                            delay_already_incremented = true;
                        } else if (j > 0 && !delay_already_incremented) {           //Para el resto de items podemos calcular el tiempo de operacion usando los finishtime anteriores. Se asume que son correlativos
                            Date finish_date_last_item = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(j - 1));
                            expected_finish_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(items_finish_times.get(j));
                            LocalDateTime new_expected_finish_time = convertToLocalDateTimeViaSqlTimestamp(now);
                            new_expected_finish_time = new_expected_finish_time.plusSeconds(((expected_finish_date.getTime() - finish_date_last_item.getTime()) / 1000)+1);
                            expected_finish_date = convertToDateViaSqlTimestamp(new_expected_finish_time);
                            System.out.println("New item finish time updated.");
                            delay_already_incremented = true;
                        }
                    }
                    }


                } catch (ParseException e) {//trycatch para dar formato a las fechas
                    e.printStackTrace();}
//                } catch (InterruptedException e) {//trycatch para el sleep
//                    e.printStackTrace();
//                }

                now = getactualtime();

                if (now.after(expected_finish_date) &&msg==null) {  //Si now es posterior a la fecha de finish time es posible que haya habido un timeout -> Se consulta el delay de inicio del plan al QoS, una única vez
                    if (!delay_already_asked) {
                        AID QoSID = new AID("QoSManagerAgent", false);
                        ACLMessage ask = new ACLMessage(ACLMessage.REQUEST);
                        ask.addReceiver(QoSID);
                        ask.setOntology("askdelay");
                        ask.setContent(batchreference);
                        try {
                            Thread.sleep(6000); //Espera 6 segundos para dar tiempo al QoS a recibir los delays de inicio del los machine agent.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        now = getactualtime();
                        send(ask);
                        ACLMessage reply = blockingReceive(MessageTemplate.MatchOntology("askdelay"));
                        String delay = reply.getContent();
                        delaynum = Long.parseLong(delay);
                        date_when_delay_was_asked = now;
                        delay_already_asked = true;
                        delay_already_incremented=false;
                    }
                    if (delay_already_asked && delay_already_incremented) { //Si ya se ha pedido el delay una vez y ha habido timeout, lanza FAILURE
                        ACLMessage timeout = new ACLMessage(ACLMessage.FAILURE);
                        AID QoSID = new AID("QoSManagerAgent", false);
                        timeout.setOntology("timeout");
                        timeout.addReceiver(QoSID);
                        timeout.setContent(batchreference);
                        System.out.println(batchreference + " batch has thrown a timeout. Checking failure with QoS Agent...");
                        send(timeout);
                        takedown_flag=true;
                    }
                }
                msg = null;
            }else{
                takedown_flag=true;
                System.out.println("Batch finish without throwing timeouts");
            }

        }
        doDelete();
    }

    protected ArrayList<String> take_finish_times(String rawdata){
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("_")));
        ArrayList<String> itemFT= new ArrayList<String>();
       for(int i=0;i<data.size();i++){
           String temp=data.get(i);
           String[] parts=temp.split("/");
            itemFT.add(parts[1]);     //Por ahora solo se coge el dato del tiempo, el cual asumimos que está bien ordenado en el plan, en un futuro tendrá disponible el itemID asociado a ese tiempo por si hay alguna modificación
       }

        return itemFT;
    }
    protected String get_batch_ID(String rawdata){
        ArrayList<String> data= new ArrayList<String>(Arrays.asList(rawdata.split("&")));
        String batchID=data.get(0);
        return batchID;
    }


    protected Date getactualtime(){
        String actualTime;
        int ano, mes, dia, hora, minutos, segundos;
        Calendar calendario = Calendar.getInstance();
        ano = calendario.get(Calendar.YEAR);
        mes = calendario.get(Calendar.MONTH) + 1;
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);
        actualTime = ano + "-" + mes + "-" + dia + "T" + hora + ":" + minutos + ":" + segundos;
        Date actualdate = null;
        try {
            actualdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(actualTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return actualdate;
    }

    protected LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
        return new java.sql.Timestamp(
                dateToConvert.getTime()).toLocalDateTime();
    }
    protected Date convertToDateViaSqlTimestamp(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }

    protected void takeDown(){
        System.out.println("Timeout agent has finished");

    }

}
