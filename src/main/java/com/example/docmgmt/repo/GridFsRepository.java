package com.example.docmgmt.repo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GridFsRepository implements AutoCloseable {
    private final MongoClient mongoClient;
    private final String dbName;
    private final String bucketName;

    public GridFsRepository(MongoClient client, String dbName, String bucketName) {
        this.mongoClient = client;
        this.dbName = dbName;
        this.bucketName = bucketName;
    }

    private GridFSBucket bucket() {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return GridFSBuckets.create(db, bucketName);
    }

    public String upload(Path path, String title) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            GridFSUploadOptions opts = new GridFSUploadOptions();
            ObjectId id = bucket().uploadFromStream(title, in, opts);
            return id.toHexString();
        }
    }

    public void download(String hexId, Path outPath) throws IOException {
        ObjectId id = new ObjectId(hexId);
        Files.createDirectories(outPath.getParent());
        try (OutputStream os = Files.newOutputStream(outPath)) {
            bucket().downloadToStream(id, os);
        }
    }

    public String saveFile(String filename, InputStream inputStream) throws IOException {
        GridFSUploadOptions opts = new GridFSUploadOptions();
        ObjectId id = bucket().uploadFromStream(filename, inputStream, opts);
        return id.toHexString();
    }

    /**
     * Đọc nội dung file từ GridFS dưới dạng String (UTF-8) – chỉ phù hợp với file text
     */
    public String readFileAsString(String hexId) throws IOException {
        ObjectId id = new ObjectId(hexId);
        try (var stream = bucket().openDownloadStream(id)) {
            // Giả định nội dung text được lưu theo UTF-8 để hiển thị Unicode (tiếng Việt) đúng
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Đọc toàn bộ bytes của file từ GridFS (dùng cho kiểm tra nhị phân / text)
     */
    public byte[] readFileBytes(String hexId) throws IOException {
        ObjectId id = new ObjectId(hexId);
        try (var stream = bucket().openDownloadStream(id)) {
            return stream.readAllBytes();
        }
    }

    /**
     * Lấy filename của file trong GridFS
     */
    public String getFilename(String hexId) {
        try {
            ObjectId id = new ObjectId(hexId);
            GridFSFile file = bucket().find(Filters.eq("_id", id)).first();
            return file != null ? file.getFilename() : hexId;
        } catch (Exception e) {
            return hexId;
        }
    }

    /**
     * Tải file từ GridFS ra file tạm trên hệ thống, trả về đường dẫn.
     * Sử dụng filename gốc (nếu có) để đoán phần mở rộng (pdf, docx,...)
     */
    public Path downloadToTempFile(String hexId) throws IOException {
        ObjectId id = new ObjectId(hexId);
        GridFSFile file = bucket().find(Filters.eq("_id", id)).first();
        String filename = file != null ? file.getFilename() : hexId;
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot != -1 && dot < filename.length() - 1) {
            ext = filename.substring(dot);
        }
        if (ext.isEmpty()) {
            ext = ".bin";
        }
        Path tmp = Files.createTempFile("docmgmt_", ext);
        try (OutputStream os = Files.newOutputStream(tmp)) {
            bucket().downloadToStream(id, os);
        }
        return tmp;
    }

    /**
     * Xóa file từ GridFS
     */
    public void delete(String hexId) {
        if (hexId == null || hexId.isEmpty()) return;
        try {
            ObjectId id = new ObjectId(hexId);
            bucket().delete(id);
        } catch (Exception e) {
            // Log error nhưng không throw để tránh lỗi khi xóa file không tồn tại
            System.err.println("Lỗi khi xóa file từ GridFS: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // no-op (client managed by Config)
    }
}

