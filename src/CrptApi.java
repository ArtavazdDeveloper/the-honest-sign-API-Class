import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

     private final int  requestLimit;
     private final long intervalMillis;
     private final Object lock = new Object();
     private AtomicInteger requestCount = new AtomicInteger(0);
     private long lastResetTime = System.currentTimeMillis();

     public CrptApi(TimeUnit timeUnit, int requestLimit){
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1)
     }
     public void createDocument(String documentJson, String signature){
        synchronized(lock){
            resetIfNecessary();
            if (requestCount.get() >= requestLimit) {
                long waitTime = lastResetTime + intervalMillis - System.currentTimeMillis();
                if (waitTime > 0) {
                    try {
                        lock.wait(waitTime);
                        resetIfNecessary();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            
            System.out.println("Document created: " + documentJson);
            requestCount.incrementAndGet();
        }
     }

     private void resetIfNecessary(){
        long currenTime = System.currentTimeMillis();
        if(currenTime - lastResetTime > intervalMillis){
            lastResetTime = currenTime;
            requestCount.set(0);
            lock.notifyAll();
        }
     }

    public static void main(String[] args) throws Exception {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);
        String documentJson = "{\"description\":{\"participantinn\":\"string\", \"doc_id\":\"string\", \"doc_status\":\"string\", \"doc_type\":\"LP_INTRODUCE_GOODS\", \"importRequest\":true, \"owner_inn\":\"string\", \"participant_inn\":\"string\", \"producer_inn\":\"string\", \"production_date\":\"2020-01-23\", \"production_type\":\"string\", \"products\":[{\"certificate_document\":\"string\", \"certificate_document_date\":\"2020-01-23\", \"certificate_document_number\":\"string\", \"owner_inn\":\"string\", \"producer_inn\":\"string\", \"production_date\":\"2020-01-23\", \"tnved_code\":\"string\", \"uit_code\":\"string\", \"uitu_code\":\"string\"}], \"reg_date\":\"2020-01-23\", \"reg_number\":\"string\"}}";
        String signature = "signature";
        crptApi.createDocument(documentJson, signature);
    }
}
