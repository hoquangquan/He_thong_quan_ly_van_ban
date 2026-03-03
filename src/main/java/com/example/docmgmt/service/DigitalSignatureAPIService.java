package com.example.docmgmt.service;

import com.example.docmgmt.repo.GridFsRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

/**
 * Service tích hợp API ký số từ các nhà cung cấp CA
 * Hỗ trợ: Vsign, VNPT-CA, FPT-CA, Viettel-CA, v.v.
 */
public class DigitalSignatureAPIService {
    private final GridFsRepository gridFsRepo;
    private final String apiBaseUrl;
    private final String apiKey;
    @SuppressWarnings("unused")
    private final String apiSecret; // Có thể dùng cho authentication trong tương lai
    private final HttpClient httpClient;
    
    /**
     * Khởi tạo service với cấu hình từ environment variables
     */
    public DigitalSignatureAPIService(GridFsRepository gridFsRepo) {
        this.gridFsRepo = gridFsRepo;
        // Đọc cấu hình từ environment variables
        this.apiBaseUrl = System.getenv().getOrDefault("DIGITAL_SIGNATURE_API_URL", "");
        this.apiKey = System.getenv().getOrDefault("DIGITAL_SIGNATURE_API_KEY", "");
        this.apiSecret = System.getenv().getOrDefault("DIGITAL_SIGNATURE_API_SECRET", "");
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    /**
     * Khởi tạo service với cấu hình tùy chỉnh
     */
    public DigitalSignatureAPIService(GridFsRepository gridFsRepo, 
                                      String apiBaseUrl, 
                                      String apiKey, 
                                      String apiSecret) {
        this.gridFsRepo = gridFsRepo;
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    /**
     * Ký số văn bản PDF thông qua API
     * @param docId ID văn bản
     * @param actionCode KY_NHAY hoặc KY_SO
     * @param signerUsername Username người ký
     * @param certificateId ID chứng thư số (từ CA)
     * @return true nếu ký thành công
     */
    public boolean signDocument(long docId, String actionCode, String signerUsername, String certificateId) {
        try {
            // 1. Kiểm tra cấu hình API
            if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
                System.err.println("Chưa cấu hình API ký số. Vui lòng set DIGITAL_SIGNATURE_API_URL");
                return false;
            }
            
            // 2. Lấy file PDF từ GridFS
            Path pdfFile = gridFsRepo.downloadToTempFile(String.valueOf(docId));
            byte[] pdfBytes = Files.readAllBytes(pdfFile);
            
            // 3. Gọi API ký số
            byte[] signedPdfBytes = callSignAPI(pdfBytes, actionCode, signerUsername, certificateId);
            
            if (signedPdfBytes == null || signedPdfBytes.length == 0) {
                System.err.println("API ký số trả về file rỗng");
                return false;
            }
            
            // 4. Lưu file đã ký vào GridFS (tạo version mới)
            Path signedPdf = Files.createTempFile("signed_", ".pdf");
            Files.write(signedPdf, signedPdfBytes);
            
            String newFileId = gridFsRepo.upload(signedPdf, "signed_" + docId + "_" + actionCode);
            
            // 5. Cập nhật latest_file_id (hoặc tạo version mới - tùy yêu cầu)
            // TODO: Cập nhật document với newFileId
            
            // Cleanup
            Files.deleteIfExists(pdfFile);
            Files.deleteIfExists(signedPdf);
            
            System.out.println("Đã ký số thành công. File ID mới: " + newFileId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Lỗi ký số: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gọi API ký số (generic - có thể customize theo từng provider)
     * 
     * Format API chuẩn (có thể thay đổi theo provider):
     * POST /api/v1/sign
     * Headers: Authorization: Bearer {apiKey}
     * Body: {
     *   "document": "base64_encoded_pdf",
     *   "certificateId": "cert_id",
     *   "signer": "username",
     *   "reason": "KY_NHAY hoặc KY_SO",
     *   "location": "Đơn vị"
     * }
     */
    private byte[] callSignAPI(byte[] pdfBytes, String actionCode, String signerUsername, String certificateId) 
            throws IOException, InterruptedException {
        
        // Encode PDF thành base64
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        
        // Tạo request body (JSON format - có thể thay đổi theo API provider)
        String requestBody = String.format(
            "{\n" +
            "  \"document\": \"%s\",\n" +
            "  \"certificateId\": \"%s\",\n" +
            "  \"signer\": \"%s\",\n" +
            "  \"reason\": \"%s\",\n" +
            "  \"location\": \"Đơn vị\",\n" +
            "  \"signatureType\": \"%s\"\n" +
            "}",
            base64Pdf,
            certificateId,
            signerUsername,
            actionCode.equals("KY_NHAY") ? "Ký nháy" : "Ký số ban hành",
            actionCode
        );
        
        // Tạo HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/api/v1/sign")) // Endpoint có thể thay đổi
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey) // Hoặc "ApiKey " + apiKey tùy provider
            .header("X-API-Key", apiKey) // Một số API dùng header này
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
        
        // Gửi request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Xử lý response
        if (response.statusCode() == 200) {
            // Parse JSON response để lấy signed document
            // Format có thể là: {"success": true, "signedDocument": "base64_encoded_pdf"}
            // Hoặc trả về trực tiếp base64 string
            
            String responseBody = response.body();
            
            // TODO: Parse JSON response (cần thêm JSON library nếu chưa có)
            // Tạm thời giả sử response là base64 string hoặc JSON với field "signedDocument"
            
            // Ví dụ với JSON đơn giản (cần thêm Gson hoặc Jackson)
            // JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            // String base64Signed = json.get("signedDocument").getAsString();
            // return Base64.getDecoder().decode(base64Signed);
            
            // Tạm thời: giả sử response là base64 string trực tiếp
            try {
                return Base64.getDecoder().decode(responseBody);
            } catch (IllegalArgumentException e) {
                // Nếu không phải base64, có thể là JSON - cần parse
                System.err.println("Response không phải base64. Cần parse JSON: " + responseBody);
                // TODO: Parse JSON và extract signedDocument
                return null;
            }
        } else {
            System.err.println("API ký số trả về lỗi: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }
    
    /**
     * Kiểm tra kết nối API
     */
    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/api/v1/health")) // Endpoint health check
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra kết nối API: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy danh sách chứng thư số có sẵn
     */
    public String[] listCertificates() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/api/v1/certificates"))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // TODO: Parse JSON response để lấy danh sách certificates
                // Tạm thời trả về mảng rỗng
                return new String[0];
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách chứng thư: " + e.getMessage());
        }
        return new String[0];
    }
}

