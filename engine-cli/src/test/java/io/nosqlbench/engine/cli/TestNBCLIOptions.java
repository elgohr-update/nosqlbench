package io.nosqlbench.engine.cli;

import io.nosqlbench.docsys.core.PathWalker;
import io.nosqlbench.nb.api.content.NBIO;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNBCLIOptions {

    @Test
    public void shouldRecognizeActivities() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "foo=wan", "start", "bar=lan"});
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
        assertThat(opts.getCommands().get(0).getCmdSpec()).isEqualTo("foo=wan;");
        assertThat(opts.getCommands().get(1).getCmdSpec()).isEqualTo("bar=lan;");
    }

    @Test
    public void shouldParseLongActivityForm() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"start", "param1=param2", "param3=param4",
                                                          "--report-graphite-to", "woot", "--report-interval", "23"});
        assertThat(opts.getCommands().size()).isEqualTo(1);
        assertThat(opts.getCommands().get(0).getCmdSpec()).isEqualTo("param1=param2;param3=param4;");
        assertThat(opts.wantsReportGraphiteTo()).isEqualTo("woot");
        assertThat(opts.getReportInterval()).isEqualTo(23);
    }

    @Test
    public void shouldRecognizeShortVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.isWantsVersionShort()).isTrue();
        assertThat(opts.wantsVersionCoords()).isFalse();
    }

    @Test
    public void shouldRecognizeVersion() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--version-coords"});
        assertThat(opts.isWantsVersionShort()).isFalse();
        assertThat(opts.wantsVersionCoords()).isTrue();
    }

    @Test
    public void shouldRecognizeScripts() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "ascriptaone", "script", "ascriptatwo"});
        assertThat(opts.getCommands()).isNotNull();
        assertThat(opts.getCommands().size()).isEqualTo(2);
        assertThat(opts.getCommands().get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.script);
        assertThat(opts.getCommands().get(0).getCmdSpec()).isEqualTo("ascriptaone");
        assertThat(opts.getCommands().get(1).getCmdType()).isEqualTo(NBCLIOptions.CmdType.script);
        assertThat(opts.getCommands().get(1).getCmdSpec()).isEqualTo("ascriptatwo");
    }

    @Test
    public void shouldRecognizeWantsActivityTypes() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--list-activity-types"});
        assertThat(opts.wantsActivityTypes()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsActivityTypes()).isFalse();
        opts = new NBCLIOptions(new String[]{"--list-drivers"});
        assertThat(opts.wantsActivityTypes()).isTrue();

    }

    @Test
    public void shouldRecognizeWantsBasicHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help"});
        assertThat(opts.wantsBasicHelp()).isTrue();
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test
    public void shouldRecognizeWantsActivityHelp() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"--help", "foo"});
        assertThat(opts.wantsTopicalHelp()).isTrue();
        assertThat(opts.wantsTopicalHelpFor()).isEqualTo("foo");
        opts = new NBCLIOptions(new String[]{"--version"});
        assertThat(opts.wantsTopicalHelp()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorSanelyWhenNoMatch() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"unrecognizable command"});
    }

    @Test
    public void testShouldRecognizeScriptParams() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "ascript", "param1=value1"});
        assertThat(opts.getCommands().size()).isEqualTo(1);
        NBCLIOptions.Cmd cmd = opts.getCommands().get(0);
        assertThat(cmd.getCmdArgs().size()).isEqualTo(1);
        assertThat(cmd.getCmdArgs()).containsKey("param1");
        assertThat(cmd.getCmdArgs().get("param1")).isEqualTo("value1");
    }

    @Test(expected = InvalidParameterException.class)
    public void testShouldErrorSanelyWhenScriptNameSkipped() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "param1=value1"});
    }

    @Test(expected = InvalidParameterException.class)
    public void testShouldErrorForMissingScriptName() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script"});
    }

    @Test
    public void testScriptInterpolation() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{"script", "script_to_interpolate", "parameter1=replaced"});
        NBCLIScriptAssembly.ScriptData s = NBCLIScriptAssembly.assembleScript(opts);
        assertThat(s.getScriptTextIgnoringParams()).contains("var foo=replaced;");
        assertThat(s.getScriptTextIgnoringParams()).contains("var bar=UNSET:parameter2");
    }

    @Test
    public void testAutoScriptCommand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "acommand" });
        NBCLIScriptAssembly.ScriptData s = NBCLIScriptAssembly.assembleScript(opts);
        assertThat(s.getScriptTextIgnoringParams()).contains("acommand script text");
    }

    @Test
    public void shouldRecognizeStartActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "start", "driver=woot" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.start);

    }

    @Test
    public void shouldRecognizeRunActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "run", "driver=runwoot" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.run);

    }

    @Test
    public void shouldRecognizeStopActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "stop", "woah" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(cmds).hasSize(1);
        assertThat(cmds.get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.stop);
        assertThat(cmds.get(0).getCmdSpec()).isEqualTo("woah");

    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowErrorForInvalidStopActivity() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "stop", "woah=woah" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
    }

    @Test
    public void shouldRecognizeAwaitActivityCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "await", "awaitme" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.await);
        assertThat(cmds.get(0).getCmdSpec()).isEqualTo("awaitme");

    }

    @Test(expected = InvalidParameterException.class)
    public void shouldThrowErrorForInvalidAwaitActivity() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "await", "awaitme=notvalid" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();

    }

    @Test
    public void shouldRecognizewaitMillisCmd() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "waitmillis", "23234" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(cmds.get(0).getCmdType()).isEqualTo(NBCLIOptions.CmdType.waitmillis);
        assertThat(cmds.get(0).getCmdSpec()).isEqualTo("23234");

    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowErrorForInvalidWaitMillisOperand() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "waitmillis", "noway" });
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();

    }

    @Test
    public void listWorkloads() {
        NBCLIOptions opts = new NBCLIOptions(new String[]{ "--list-workloads"});
        List<NBCLIOptions.Cmd> cmds = opts.getCommands();
        assertThat(opts.wantsScenariosList());
    }


    @Test
    public void clTest() {
        String dir= "./";
        URL resource = getClass().getClassLoader().getResource(dir);
        assertThat(resource);
        Path basePath = NBIO.getFirstLocalPath(dir);
        List<Path> yamlPathList = PathWalker.findAll(basePath).stream().filter(f -> f.toString().endsWith(".yaml")).collect(Collectors.toList());
        assertThat(yamlPathList);
    }


}
