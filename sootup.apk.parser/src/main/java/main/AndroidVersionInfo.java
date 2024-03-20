package main;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

public class AndroidVersionInfo {

  public int sdkTargetVersion = -1;
  public int minSdkVersion = -1;
  public int platformBuildVersionCode = -1;

  public static AndroidVersionInfo get(InputStream manifestIS) {
    final AndroidVersionInfo versionInfo = new AndroidVersionInfo();
    final AxmlVisitor axmlVisitor =
        new AxmlVisitor() {
          private String nodeName = null;

          @Override
          public void attr(String ns, String name, int resourceId, int type, Object obj) {
            super.attr(ns, name, resourceId, type, obj);

            if (nodeName != null && name != null) {
              if (nodeName.equals("manifest")) {
                if (name.equals("platformBuildVersionCode")) {
                  versionInfo.platformBuildVersionCode = Integer.valueOf("" + obj);
                }
              } else if (nodeName.equals("uses-sdk")) {
                // Obfuscated APKs often remove the attribute names and use the resourceId instead
                // Therefore it is better to check for both variants
                if (name.equals("targetSdkVersion") || (name.isEmpty() && resourceId == 16843376)) {
                  versionInfo.sdkTargetVersion = Integer.valueOf(String.valueOf(obj));
                } else if (name.equals("minSdkVersion")
                    || (name.isEmpty() && resourceId == 16843276)) {
                  versionInfo.minSdkVersion = Integer.valueOf(String.valueOf(obj));
                }
              }
            }
          }

          @Override
          public NodeVisitor child(String ns, String name) {
            nodeName = name;
            return this;
          }
        };
    try {
      AxmlReader xmlReader = new AxmlReader(IOUtils.toByteArray(manifestIS));
      xmlReader.accept(axmlVisitor);
    } catch (Exception e) {
    }
    return versionInfo;
  }
}
