package edu.colorado.cires.mgg.quakenet.lambda.initiator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitiatorLambdaHandler implements RequestStreamHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiatorLambdaHandler.class);

  @Override
  public void handleRequest(InputStream in, OutputStream out, Context context) throws IOException {

  }
}
