package edu.colorado.cires.mgg.quakenet.lambda.eventdetailsgrabber;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.colorado.cires.mgg.quakenet.message.EventDetailGrabberMessage;
import edu.colorado.cires.mgg.quakenet.util.ObjectMapperCreator;
import org.junit.jupiter.api.Test;

class EventDetailsGrabberDlqExecutorTest {


  @Test
  void test() throws Exception {

    String bucketName = "my-bucket";
    String topicArn = "topicArn";

    EventDetailsGrabberProperties properties = new EventDetailsGrabberProperties();
    properties.setBucketName(bucketName);
    properties.setTopicArn(topicArn);

    ObjectMapper objectMapper = ObjectMapperCreator.create();
    S3Doer s3Doer = mock(S3Doer.class);
    Notifier notifier = mock(Notifier.class);
    EventDetailsGrabberDlqExecutor executor = new EventDetailsGrabberDlqExecutor(s3Doer, notifier, properties);

    String eventId = "us7000fxq2";
    String date = "2021-11-28";

    EventDetailGrabberMessage message = EventDetailGrabberMessage.Builder.builder().withEventId(eventId).withDate(date).build();
    ObjectNode json = objectMapper.createObjectNode();
    json.put("body", objectMapper.writeValueAsString(message));

    executor.execute(json.toString(), message);

    verify(s3Doer, times(1)).saveFile(
        eq(bucketName),
        eq("downloads/2021/11/2021-11-28/us7000fxq2/event-error-2021-11-28-us7000fxq2.json.gz"),
        eq(json.toString()));

    verify(notifier, times(1)).notify(eq(topicArn), eq(message));

    verifyNoMoreInteractions(notifier);
  }


}