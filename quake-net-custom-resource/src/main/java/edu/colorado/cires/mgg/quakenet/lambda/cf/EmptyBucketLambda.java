package edu.colorado.cires.mgg.quakenet.lambda.cf;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.sunrun.cfnresponse.CfnRequest;
import com.sunrun.cfnresponse.CfnResponseSender;
import com.sunrun.cfnresponse.Status;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;


public class EmptyBucketLambda implements RequestHandler<CfnRequest<EmptyBucketProperties>, Void> {

  static final Logger LOGGER = LoggerFactory.getLogger(EmptyBucketLambda.class);

  private final S3Client s3 = S3Client.builder().build();
  private final Handler handler = new Handler(s3, () -> new CfnResponseSender());

  private static final int timeoutSeconds = Integer.parseInt(System.getenv("TIMEOUT_SECONDS"));

  @Override
  public Void handleRequest(CfnRequest<EmptyBucketProperties> event, Context context) {
    handler.handleRequest(event, context);
    return null;
  }

  static class Handler {

    private final S3Client s3;
    private final Supplier<CfnResponseSender> senderFactory;

    public Handler(S3Client s3, Supplier<CfnResponseSender> senderFactory) {
      this.s3 = s3;
      this.senderFactory = senderFactory;
    }

    public void handleRequest(CfnRequest<EmptyBucketProperties> event, Context context) {
      try {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
          String requestType = event.getRequestType();
          switch (requestType) {
            case "Delete":
              handleDelete(event, context, requestType);
              break;
            case "Create":
            case "Update":
            default:
              sendResponse(event, context, Status.SUCCESS, requestType);
              break;
          }
          return null;
        });
        try {
          future.get(timeoutSeconds - 1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          future.cancel(true);
          throw e;
        } finally {
          executor.shutdownNow();
        }


      } catch (Exception e) {
        LOGGER.error("Custom resource action failed", e);
        sendResponse(event, context, Status.FAILED, e.getMessage());
      }
    }

    private void sendResponse(CfnRequest<EmptyBucketProperties> event, Context context, Status status, String reason) {
      try {
        CfnResponseSender sender = senderFactory.get();
        sender.send(event, status, context, reason, null, "1");
      } catch (Exception e) {
        LOGGER.error("Unable to send custom resource response", e);
      }
    }

    private void handleDelete(CfnRequest<EmptyBucketProperties> event, Context context, String type) throws Exception {
      EmptyBucketProperties properties = event.getResourceProperties();
      LOGGER.info("Deleting {}", properties);
      BucketDrain.emptyBucket(s3, properties.getBucketName());
      LOGGER.info("Deleted {}", properties);
      sendResponse(event, context, Status.SUCCESS, type);
    }


  }

}
