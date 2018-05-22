import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.net.*;
import java.lang.String;
import java.nio.file.Paths;
import java.net.URI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ContractExecutorServiceSecurityTestCode extends SmartContract {

    private int total;

    public ContractExecutorServiceSecurityTestCode() throws Exception {
        String name = Thread.currentThread().getName();
        System.out.println("The constructor has been invoked. Thread: " + name);
//        System.out.println("Balance is " + getBalance("accXpfvxnZa8txuxpjyPqzBaqYPHqYu2rwn34lL8rjI=", "cs") + "Thread: " + name);
    }

    @Override
    public void initialize(){
        System.out.println("The init method has been invoked");
        total = 1;
    }

    public void openSocket(int port) throws Exception {
        System.out.println("Trying to open socket...");
        new ServerSocket(port);
        System.out.println("Opened");
    }

    public void setTotal(int value) {
        total = value;
    }

    public int getTotal() {
        return total;
    }

    public void createFile() throws Exception {
        try {
            Files.createFile(Paths.get(new URI("file:///./some.file")));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void killProcess() {
        System.exit(-1);
    }

    public void killThread() {
        Thread.currentThread().stop();
    }

    public void newThread() {
        new Thread(() -> {
            System.out.println("new Thread");
        });
    }

    public void getBalance() throws Exception {
        System.out.println("getBalance()");
        BigDecimal balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}