package org.ananas.acceptance;

import com.jayway.jsonpath.JsonPath;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ananas.TestHelper;
import org.ananas.acceptance.helper.AcceptanceTestBase;
import org.ananas.acceptance.helper.DataViewerHelper;
import org.ananas.cli.Main;
import org.junit.Assert;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;

public class MySQLSource extends AcceptanceTestBase {
  @Test
  public void exploreMySQLSource() {
    exit.expectSystemExitWithStatus(0);

    URL project = TestHelper.getResource("test_projects/mysql");

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            List<Map<String, String>> fields = JsonPath.read(json, "$.data.schema.fields");
            Assert.assertTrue(fields.size() > 0);
          }
        });

    Main.main(
        new String[] {
          "explore",
          "-p",
          project.getPath(),
          "5d56c902d4dbe821c135eb94",
          "-n",
          "0",
          "--size",
          "5",
          "-m",
          "PASSWORD=" + props.getProperty("mysql.password"),
          "-m",
          "DB=classicmodels",
          "-m",
          "HOST=" + props.getProperty("mysql.host"),
        });
  }

  @Test
  public void testRunDataViewer() {
    String stepId = "5d56cf60d4dbe821c135ebb9";

    exit.expectSystemExitWithStatus(0);

    exit.checkAssertionAfterwards(
        new Assertion() {
          public void checkAssertion() {
            String json = systemOutRule.getLog();
            // TODO: test stdout here
            int code = JsonPath.read(json, "$.code");
            Assert.assertEquals(200, code);

            String jobId = JsonPath.read(json, "$.data.jobid");
            Assert.assertNotNull(jobId);

            // check data viewer job result
            String result =
                DataViewerHelper.getViewerJobDataWithDefaultDB(
                    "SELECT * FROM PCOLLECTION", jobId, stepId);
            int resultCode = JsonPath.read(result, "$.code");
            Assert.assertEquals(200, resultCode);

            List<Map<String, String>> fields = JsonPath.read(result, "$.data.schema.fields");
            Assert.assertTrue(fields.size() > 0);

            List<Object> data = JsonPath.read(result, "$.data.data");
            Assert.assertTrue(data.size() > 0);

            System.out.println(result);
          }
        });

    URL project = TestHelper.getResource("test_projects/mysql");

    Main.main(
        new String[] {
          "run",
          "-p",
          project.getPath(),
          stepId,
          "-m",
          "PASSWORD=" + props.getProperty("mysql.password"),
          "-m",
          "DB=classicmodels",
          "-m",
          "HOST=" + props.getProperty("mysql.host"),
        });
  }
}
