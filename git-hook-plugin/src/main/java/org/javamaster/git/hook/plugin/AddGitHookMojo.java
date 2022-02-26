package org.javamaster.git.hook.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * 负责在每次项目编译时把git钩子复制到项目的.git目录下
 *
 * @author yudong
 * @date 2022/2/26
 */
@Mojo(name = "add-git-hook", defaultPhase = LifecyclePhase.COMPILE)
public class AddGitHookMojo extends AbstractMojo {
    public static final int BUFFER_SIZE = 4096;
    /**
     * pom文件所在的目录
     */
    @Parameter(defaultValue = "${project.basedir}")
    private File basedir;
    /**
     * git测试分支名称
     */
    @Parameter(required = true)
    private String gitTestBranchName;

    @Override
    public void execute() {
        getLog().info("复制git hooks到.git目录");
        File parentFile = basedir.getParentFile();
        File dotGitPath = findDotGitPath(parentFile);
        if (dotGitPath == null) {
            getLog().info("未找到.git目录");
            return;
        }
        copyHook(dotGitPath, "pre-push");
    }

    private void copyHook(File dotGitPath, String hookFileName) {
        try {
            File hookPath = new File(dotGitPath + "/hooks");
            File hookFilePath = new File(hookPath, hookFileName);
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(hookFileName);

            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
            copy(Objects.requireNonNull(stream), out);
            stream.close();

            String s = new String(out.toByteArray(), StandardCharsets.UTF_8);
            s = s.replace("$gitTestBranchName", gitTestBranchName);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            Files.copy(byteArrayInputStream, hookFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            byteArrayInputStream.close();

            getLog().info("完成复制" + hookFileName + "钩子到" + hookPath.getAbsolutePath() + "目录");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File findDotGitPath(File path) {
        if (path == null) {
            return null;
        }
        for (File file : Objects.requireNonNull(path.listFiles())) {
            if (".git".equals(file.getName())) {
                return file;
            }
        }
        return findDotGitPath(path.getParentFile());
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }
}
