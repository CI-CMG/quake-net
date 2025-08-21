package edu.colorado.cires.mgg.quakenet.s3.util;

import edu.colorado.cires.mgg.quakenet.message.InfoFile;
import edu.colorado.cires.mgg.quakenet.message.ReportInfoFile;
import java.util.Optional;

public interface InfoFileS3Actions {

  boolean isFileExists(String bucketName, String key);

  Optional<InfoFile> readInfoFile(String bucketName, String key);

  Optional<ReportInfoFile> readReportInfoFile(String bucketName, String key);

  void saveInfoFile(String bucketName, String key, InfoFile infoFile);

  void saveReportInfoFile(String bucketName, String key, ReportInfoFile infoFile);
}
