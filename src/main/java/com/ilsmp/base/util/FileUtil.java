package com.ilsmp.base.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

/**
 * 文件工具类
 *
 * @author wangzhao10
 */
@Slf4j
public class FileUtil {
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "文件上传子文件")
    public static class SubFile {

        @Schema(description = "子文件的EDB命名规则下的名称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false,
                type = "String", example = "20211001_I_1312_0008.txt",defaultValue = "20211001_I_1312_0008.txt")
        private String subFileName;

        @Schema(description = "子文件的大小字节树", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false,
                type = "int", example = "123456",defaultValue = "123456")
        private Integer subFileSize;

        @Schema(description = "子文件的生成时间", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false,
                type = "Date", example = "2021-10-01 10:10:10",defaultValue = "2021-10-01 10:10:10")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date subFileCreateTime;

        @Schema(description = "子文件的发送时间", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false,
                type = "Date", example = "2021-10-01 10:10:10",defaultValue = "2021-10-01 10:10:10")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date subFileSendTime;

    }


    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final ExecutorService EXECUTOR =
            ExecutorUtil.getInstance().getSingleExecutor("fileUtil");

    /**
     * 左填充
     *
     * @param str
     * @param length
     * @param ch
     * @return
     */
    public static String leftPad(String str, int length, char ch) {
        if (str.length() >= length) {
            return str;
        }
        char[] chs = new char[length];
        Arrays.fill(chs, ch);
        char[] src = str.toCharArray();
        System.arraycopy(src, 0, chs, length - src.length, src.length);
        return new String(chs);
    }

    /**
     * 删除文件
     *
     * @param fileName
     *         待删除的完整文件名
     * @return boolean
     */
    public static boolean delete(String fileName) {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            result = f.delete();
        } else {
            result = true;
        }
        return result;
    }

    public static boolean deleteByPre(String dirPath, final String prefix, final String suffix) {
        ArrayList<File> files = FileUtil.getDirFiles(dirPath, prefix,
                suffix);
        return deleteByList(dirPath, files);
    }

    public static boolean deleteByList(String dirPath, List<File> files) {
        AtomicBoolean delete = new AtomicBoolean(true);
        files.forEach(file -> {
            if (!file.delete()) {
                delete.set(false);
                return;
            }
            ;
        });
        return delete.get();
    }

    /***
     * 递归获取指定目录下的所有的文件（不包括文件夹）
     *
     * @param dirPath
     * @return ArrayList
     */
    public static ArrayList<File> getAllFiles(String dirPath) {
        File dir = new File(dirPath);
        ArrayList<File> files = new ArrayList<File>();
        if (dir.isDirectory()) {
            File[] fileArr = dir.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File f = fileArr[i];
                if (f.isFile()) {
                    files.add(f);
                } else {
                    files.addAll(getAllFiles(f.getPath()));
                }
            }
        }
        return files;
    }

    /**
     * 获取指定目录下的所有文件(不包括子文件夹)
     *
     * @return ArrayList
     */
    public static ArrayList<File> getDirFiles(String dirPath) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles();
        assert fileArr != null;
        return getFiles(fileArr);
    }

    /**
     * 获取指定目录下的所有文件(包括文件夹)
     *
     * @return ArrayList
     */
    public static List<File> getDirAllFiles(String dirPath) {
        File path = new File(dirPath);
        File[] files = path.listFiles();
        if (files != null) {
            return Arrays.asList(files);
        }
        return null;
    }

    /**
     * 获取文件目录下的所有文件，剔除文件夹
     *
     * @return ArrayList
     */
    private static ArrayList<File> getFiles(File[] fileArr) {
        ArrayList<File> files = new ArrayList<>();
        assert fileArr != null;
        for (File f : fileArr) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 获取指定目录下特定文件后缀名的文件列表(不包括子文件夹)
     *
     * @param dirPath
     *         目录路径
     * @param suffix
     *         文件后缀
     */
    public static ArrayList<File> getDirFiles(String dirPath, final String prefix, final String suffix) {
        File path = new File(dirPath);
        File[] fileArr = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowerName = name.toLowerCase();
                if (prefix == null && suffix != null) {
                    String lowerSuffix = suffix.toLowerCase();
                    return lowerName.endsWith(lowerSuffix);
                } else if (prefix != null && suffix == null) {
                    String lowerPrefix = prefix.toLowerCase();
                    return lowerName.startsWith(lowerPrefix);
                } else if (prefix != null && suffix != null) {
                    String lowerPrefix = prefix.toLowerCase();
                    String lowerSuffix = suffix.toLowerCase();
                    return lowerName.startsWith(lowerPrefix) && lowerName.endsWith(lowerSuffix);
                } else {
                    return true;
                }
            }
        });
        assert fileArr != null;
        return getFiles(fileArr);
    }

    /**
     * 读取文件内容
     *
     * @param fileName
     *         待读取的完整文件名
     * @return 文件内容
     * @throws IOException
     */
    public static String read(String fileName) throws IOException {
        File f = new File(fileName);
        FileInputStream fs = new FileInputStream(f);
        String result = null;
        byte[] b = new byte[fs.available()];
        int read = fs.read(b);
        fs.close();
        result = new String(b);
        return result;
    }

    /**
     * 写文件
     *
     * @param fileName
     *         目标文件名
     * @param fileContent
     *         写入的内容
     * @return boolean
     * @throws IOException
     */
    public static boolean write(String fileName, String fileContent) throws IOException {
        boolean result = false;
        File f = new File(fileName);
        FileOutputStream fs = new FileOutputStream(f);
        byte[] b = fileContent.getBytes();
        fs.write(b);
        fs.flush();
        fs.close();
        result = true;
        return result;
    }

    /**
     * 追加内容到指定文件
     *
     * @param fileName
     * @param fileContent
     * @return boolean
     * @throws IOException
     */
    public static boolean append(String fileName, String fileContent)
            throws IOException {
        boolean result = false;
        File f = new File(fileName);
        if (f.exists()) {
            RandomAccessFile rFile = new RandomAccessFile(f, "rw");
            byte[] b = fileContent.getBytes();
            long originLen = f.length();
            rFile.setLength(originLen + b.length);
            rFile.seek(originLen);
            rFile.write(b);
            rFile.close();
        }
        result = true;
        return result;
    }

    /**
     * 拆分文件
     *
     * @param fileDir
     *         + fileName 待拆分的完整文件名
     * @param byteSize
     *         按多少字节大小拆分
     * @return 拆分后的文件名列表
     * @throws IOException
     */
    public static List<SubFile> splitBySize(String fileDir, String fileName, int byteSize, String resetName) {
        return splitBySize(fileDir, fileName, byteSize, resetName, fileName);
    }

    public static List<SubFile> splitBySize(String fileDir, String fileName, int byteSize, String resetName,
                                            String partFileSuffix) {
        if (StringUtils.isEmpty(partFileSuffix)) {
            partFileSuffix = ".part";
        }
        List<SubFile> parts = new ArrayList<>();
        File file = new File(fileDir + fileName);
        int count = (int) Math.ceil(file.length() / (double) byteSize);
        int countLen = (count + "").length();
        for (int i = 0; i < count; i++) {
            String partFileName = (resetName != null ? resetName : (fileDir + fileName)) + "_"
                    + leftPad((i + 1) + "", 4, '0') + partFileSuffix;
            EXECUTOR.execute(new SplitRunnable(byteSize, i * (long) byteSize,
                    (resetName != null ? (fileDir + partFileName) : partFileName), file));
            SubFile subFile = new SubFile();
            subFile.setSubFileName(partFileName);
            subFile.setSubFileCreateTime(new Date());
            if (i < count - 1) {
                subFile.setSubFileSize(byteSize);
            } else {
                subFile.setSubFileSize((int) (file.length() - byteSize * (count - 1)));
            }
            parts.add(subFile);
        }
        return parts;
    }

    /**
     * 合并文件
     *
     * @param fileDir
     *         拆分文件所在目录名
     * @param partFileSuffix
     *         拆分文件后缀名
     * @param partFileSize
     *         拆分文件的字节数大小
     * @param mergeFileName
     *         合并后的文件名
     * @throws IOException
     */
    public static void mergePartFiles(String fileDir, int partFileSize, String mergeFileName, String partFileSuffix)
            throws IOException {
        mergePartFiles(fileDir, partFileSize, mergeFileName, partFileSuffix, null);
    }

    public static void mergePartFiles(String fileDir, int partFileSize, String mergeFileName, String partFileSuffix,
                                      String partFilePrefix) throws IOException {
        ArrayList<File> partFiles = FileUtil.getDirFiles(fileDir, partFilePrefix,
                partFileSuffix);
        if (partFiles.size() > 0) {
            partFiles.sort(new FileComparator());
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileDir + mergeFileName,
                    "rw");
            randomAccessFile.setLength((long) partFileSize * (partFiles.size() - 1)
                    + partFiles.get(partFiles.size() - 1).length());
            randomAccessFile.close();
//            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
//                    1, 1, 1, TimeUnit.SECONDS,
//                    new ArrayBlockingQueue<Runnable>(partFiles.size()));
            for (int i = 0; i < partFiles.size(); i++) {
                EXECUTOR.execute(new MergeRunnable((long) i * partFileSize,
                        fileDir + mergeFileName, partFiles.get(i)));
                if (i == partFiles.size() - 1) {
                    EXECUTOR.execute(() -> deleteByList(fileDir, partFiles));
                }
            }
        }
    }

    /**
     * 根据文件名，比较文件
     *
     * @author zjh
     */
    private static class FileComparator implements Comparator<File> {
        public int compare(File o1, File o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    /**
     * 分割处理Runnable
     *
     * @author zjh
     */
    private static class SplitRunnable implements Runnable {
        int byteSize;
        String partFileName;
        File originFile;
        long startPos;

        public SplitRunnable(int byteSize, long startPos, String partFileName,
                             File originFile) {
            // 传参时要强制转化long型
            this.startPos = startPos;
            this.byteSize = byteSize;
            log.info(startPos + "--------startPos不超long-------");
            this.partFileName = partFileName;
            this.originFile = originFile;
        }

        @Override
        public void run() {
            RandomAccessFile rFile;
            OutputStream os;
            try {
                rFile = new RandomAccessFile(originFile, "r");
                byte[] b = new byte[byteSize];
                rFile.seek(startPos);// 移动指针到每“段”开头
                int s = rFile.read(b);
                os = new FileOutputStream(partFileName);
                os.write(b, 0, s);
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并处理Runnable
     *
     * @author zjh
     */
    private static class MergeRunnable implements Runnable {
        long startPos;
        String mergeFileName;
        File partFile;

        public MergeRunnable(long startPos, String mergeFileName, File partFile) {
            this.startPos = startPos;
            this.mergeFileName = mergeFileName;
            this.partFile = partFile;
        }

        @Override
        public void run() {
            RandomAccessFile rFile;
            try {
                rFile = new RandomAccessFile(mergeFileName, "rw");
                rFile.seek(startPos);
                FileInputStream fs = new FileInputStream(partFile);
                byte[] b = new byte[fs.available()];
                int read = fs.read(b);
                fs.close();
                rFile.write(b);
                rFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File createDirectory(String path) throws Exception {
        File file = new File(path);
        return FileUtil.createDirectory(file);
    }

    public static File createDirectory(File file) throws Exception {
        boolean bo = file.mkdirs();
        if (!bo) {
            throw new Exception("创建文件目录失败！");
        }
        return file;
    }

    public static File createFile(String path) throws Exception {
        path = FileUtil.treatFilePath(path).trim();
        // path=Util.trimStartString(path, "/");
        if (FileUtil.isDirectory(path)) {
            return FileUtil.createDirectory(path);
        }
        File file = new File(path);
        return FileUtil.createFile(file);
    }

    public static File createFile(File file) throws Exception {
        if (FileUtil.isDirectory(file)) {
            return FileUtil.createDirectory(file);
        }
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
            } catch (IOException e) {
                String path = file.getAbsolutePath();
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean isDirectory(File file) {
        String path = file.getAbsolutePath();
        return FileUtil.isDirectory(path);
    }

    public static boolean isDirectory(String path) {
        path = path == null ? "" : path;
        if (path.equals("")) {
            return true;
        }
        boolean isDir = false;
        path = FileUtil.treatFilePath(path);
        if (path.contains("/")) {
            String[] lines = path.split("/");
            if (lines.length == 0) {
                return true;
            }
            String last = lines[lines.length - 1];
            isDir = last.indexOf(".") <= 0;
        }
        return isDir;
    }

    public static File initFile(File file) throws Exception {
        return FileUtil.writeToFile("", file, false);
    }

    public static File writeToFile(File srcFile, File destFile) throws Exception {
        List<String> list = FileUtil.readFile(srcFile);
        return FileUtil.writeToFile(list, destFile);
    }

    public static File writeToFile(String[] lines, File file) throws Exception {
        List<String> list = new ArrayList<String>(Arrays.asList(lines));
        return FileUtil.writeToFile(list, file);
    }

    public static File writeToFile(List<String> list, File file) throws Exception {
        boolean isFirstLine = true;
        if (list.size() == 0) {
            FileUtil.initFile(file);
            return file;
        }
        for (String line : list) {
            if (isFirstLine) {
                file = writeToFile(line, file, false);
                isFirstLine = false;
            } else {
                file = appendToFile(line, file);
            }
        }
        return file;
    }

    public static File writeToFile(String line, File file, boolean isAppend) throws Exception {
        file = FileUtil.createFile(file);
        if (line == null) {
            return file;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, isAppend), StandardCharsets.UTF_8));
            bw.write(line);
            bw.flush();
            if (line.length() != 0) {
                // line no equals ""
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            return file;
        }
        return file;
    }

    public static File appendToFile(List<String> list, File file) throws Exception {
        for (String line : list) {
            file = FileUtil.appendToFile(line, file);
        }
        return file;
    }

    public static File appendToFile(String line, File file) throws Exception {
        return writeToFile(line, file, true);
    }

    public static List<String> readFile(File file) throws Exception {
        file = FileUtil.createFile(file);
        List<String> list = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8));
            String line = br.readLine();
            while (line != null) {
                list.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String treatFilePath(String path) {
        path = path.replaceAll("[/\\\\]{2,}", "/");
        return path;
    }

    public static final String loadFromFile(String filePath) {
        BufferedReader reader = null;
        StringBuffer laststr = new StringBuffer("");
        try {
            if (filePath.startsWith("classpath:")) {
                filePath = ClassLoader.getSystemResource("").getPath() + File.separator + filePath.substring(10);
                LOGGER.debug("Current json path is <{}>.", filePath);
            }
            InputStreamReader inputStreamReader;
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            }
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            return laststr.toString();
        } catch (IOException e) {
            LOGGER.error("Load file from file path <{}> failed.", filePath, e);
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.debug("The reader object has already been closed.", e);
                }
            }
        }
    }

    public static String getFilePath(String filePath) {
        filePath = treatFilePath(filePath);
        if (filePath.contains(".")) {
            String[] split = getFullFilePath(filePath).split("/");
            filePath = String.join("/", split);
        }
        return filePath;
    }

    public static String getFullFilePath(String filePath) {
        if (filePath.startsWith("classpath:")) {
            filePath = ClassLoader.getSystemResource("").getPath() + File.separator + filePath.substring(10);
            LOGGER.debug("Current json path is <{}>.", filePath);
        }
        return filePath;
    }

}
