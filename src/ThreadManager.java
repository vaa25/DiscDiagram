import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vaa25 on 11.10.2015.
 */
public class ThreadManager {
    public static ExecutorService service = Executors.newCachedThreadPool();
}
