package com.example.docmgmt.gui;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Panel hiển thị PDF bằng cách render từng trang thành hình ảnh.
 * Hỗ trợ cả PDF text lẫn PDF scan bằng cách render từng trang (lazy loading)
 * và tự động giảm DPI nếu gặp lỗi thiếu bộ nhớ.
 */
public class PDFViewerPanel extends JPanel {
    private PDDocument document;
    private int currentPage = 0;
    private int totalPages = 0;
    private JLabel pageLabel;
    private JPanel imagePanel;      // Hiển thị 1 trang
    private JPanel allPagesPanel;   // Hiển thị liên tục nhiều trang
    private JScrollPane scrollPane;
    private PDFRenderer renderer;
    private JLabel statusLabel;
    private SwingWorker<BufferedImage, Void> renderWorker;
    private SwingWorker<Void, Void> renderAllWorker;
    private BufferedImage currentImage;
    private boolean continuousMode = false;
    private JPanel controlPanel;

    public PDFViewerPanel() {
        setLayout(new BorderLayout());
        
        // Panel hiển thị hình ảnh
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.GRAY);

        // Panel hiển thị toàn bộ trang (scroll liên tục)
        allPagesPanel = new JPanel();
        allPagesPanel.setLayout(new BoxLayout(allPagesPanel, BoxLayout.Y_AXIS));
        allPagesPanel.setBackground(Color.DARK_GRAY);
        
        scrollPane = new JScrollPane(imagePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Panel điều khiển (nút chuyển trang, số trang)
        controlPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton prevBtn = new JButton("◀ Trước");
        JButton nextBtn = new JButton("Sau ▶");
        pageLabel = new JLabel("Trang 0 / 0");
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        
        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateDisplay();
            }
        });
        
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateDisplay();
            }
        });
        
        buttonPanel.add(prevBtn);
        buttonPanel.add(pageLabel);
        buttonPanel.add(nextBtn);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        controlPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Load PDF từ byte array với cơ chế render từng trang.
     */
    public boolean loadPDF(byte[] pdfData) {
        continuousMode = false;
        try {
            closeDocument();

            if (pdfData == null || pdfData.length == 0) {
                showError("Dữ liệu PDF trống");
                return false;
            }
            
            // PDFBox 3.x: dùng Loader.loadPDF() thay vì PDDocument.load()
            document = Loader.loadPDF(pdfData);
            totalPages = document.getNumberOfPages();
            
            if (totalPages == 0) {
                showError("PDF không có trang nào");
                return false;
            }
            
            renderer = new PDFRenderer(document);
            currentPage = 0;
            updateDisplay();
            return true;
            
        } catch (OutOfMemoryError oom) {
            showError("PDF quá lớn, không đủ bộ nhớ để hiển thị. Vui lòng dùng nút 'Xuất file'.");
            System.err.println("PDFViewerPanel OutOfMemoryError: " + oom.getMessage());
            closeDocument();
            return false;
        } catch (Exception e) {
            showError("Không thể đọc PDF: " + e.getMessage());
            e.printStackTrace();
            closeDocument();
            return false;
        }
    }

    /**
     * Load PDF từ InputStream
     */
    public boolean loadPDF(java.io.InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        return loadPDF(data);
    }

    /**
     * Load và hiển thị toàn bộ các trang (chế độ cuộn liên tục).
     * Lưu ý: dùng DPI thấp hơn để tránh tràn bộ nhớ.
     */
    public boolean loadPDFContinuous(byte[] pdfData) {
        continuousMode = true;
        try {
            closeDocument();

            if (pdfData == null || pdfData.length == 0) {
                showError("Dữ liệu PDF trống");
                return false;
            }

            document = Loader.loadPDF(pdfData);
            totalPages = document.getNumberOfPages();

            if (totalPages == 0) {
                showError("PDF không có trang nào");
                return false;
            }

            renderer = new PDFRenderer(document);
            renderAllPages();
            return true;
        } catch (OutOfMemoryError oom) {
            showError("PDF quá lớn, không đủ bộ nhớ để hiển thị. Vui lòng dùng nút 'Xuất file'.");
            System.err.println("PDFViewerPanel OutOfMemoryError (continuous): " + oom.getMessage());
            closeDocument();
            return false;
        } catch (Exception e) {
            showError("Không thể đọc PDF: " + e.getMessage());
            e.printStackTrace();
            closeDocument();
            return false;
        }
    }

    private synchronized void updateDisplay() {
        if (continuousMode) {
            // Trong chế độ cuộn liên tục, không dùng updateDisplay của single-page
            return;
        }
        cancelRender();

        // Đảm bảo scrollPane đang hiển thị panel single-page
        if (scrollPane.getViewport().getView() != imagePanel) {
            scrollPane.setViewportView(imagePanel);
        }

        imagePanel.removeAll();

        if (document == null || renderer == null || totalPages == 0) {
            imagePanel.revalidate();
            imagePanel.repaint();
            return;
        }

        if (currentPage < 0 || currentPage >= totalPages) {
            currentPage = Math.max(0, Math.min(totalPages - 1, currentPage));
        }

        pageLabel.setText(String.format("Trang %d / %d", currentPage + 1, totalPages));
        statusLabel.setText("Đang render trang " + (currentPage + 1) + "...");
        statusLabel.setForeground(Color.BLUE);

        JLabel loading = new JLabel("Đang tải trang...");
        loading.setHorizontalAlignment(SwingConstants.CENTER);
        loading.setVerticalAlignment(SwingConstants.CENTER);
        imagePanel.add(loading, BorderLayout.CENTER);
        imagePanel.revalidate();
        imagePanel.repaint();

        renderWorker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                int[] dpis = {100, 96, 72, 150, 120, 200};
                for (int dpi : dpis) {
                    try {
                        return renderer.renderImageWithDPI(currentPage, dpi, ImageType.RGB);
                    } catch (OutOfMemoryError oom) {
                        System.gc();
                        System.err.println("Thiếu bộ nhớ khi render DPI " + dpi + ", thử mức thấp hơn...");
                    } catch (Exception ex) {
                        System.err.println("Lỗi render trang " + (currentPage + 1) + " ở DPI " + dpi + ": " + ex.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    currentImage = get();
                } catch (Exception e) {
                    currentImage = null;
                }

                imagePanel.removeAll();
                if (currentImage != null) {
                    // Tự động scale image để fit với chiều rộng viewport
                    int viewportWidth = scrollPane.getViewport().getWidth();
                    // Nếu viewport chưa có kích thước, dùng kích thước mặc định (800px)
                    if (viewportWidth <= 0) {
                        viewportWidth = 800;
                    }
                    BufferedImage scaledImage = scaleImageToFitWidth(currentImage, viewportWidth);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage)) {
                        @Override
                        public Dimension getPreferredSize() {
                            if (scaledImage != null) {
                                return new Dimension(scaledImage.getWidth(), scaledImage.getHeight());
                            }
                            return super.getPreferredSize();
                        }
                    };
                    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    imagePanel.add(imageLabel, BorderLayout.CENTER);
                    statusLabel.setText("Hiển thị ở DPI " + estimateDpi(currentImage) + " (đã scale để vừa màn hình)");
                    statusLabel.setForeground(Color.GRAY);
                } else {
                    showError("Không thể render trang " + (currentPage + 1) + ". Vui lòng dùng nút 'Xuất file'.");
                    statusLabel.setText("Lỗi render");
                    statusLabel.setForeground(Color.RED);
                }
                imagePanel.revalidate();
                imagePanel.repaint();
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        };
        renderWorker.execute();
    }

    private void renderAllPages() {
        cancelRender();
        cancelRenderAll();

        // Chuyển viewport sang panel hiển thị liên tục
        scrollPane.setViewportView(allPagesPanel);
        allPagesPanel.removeAll();
        statusLabel.setText("Đang render toàn bộ trang...");
        statusLabel.setForeground(Color.BLUE);
        controlPanel.setVisible(false); // Ẩn nút trước/sau vì không cần trong chế độ cuộn

        renderAllWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int[] dpis = {100, 96, 72, 150, 120, 200};
                for (int page = 0; page < totalPages; page++) {
                    BufferedImage img = null;
                    for (int dpi : dpis) {
                        try {
                            img = renderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                            break;
                        } catch (OutOfMemoryError oom) {
                            System.gc();
                            System.err.println("Thiếu bộ nhớ trang " + (page + 1) + " DPI " + dpi + ", thử thấp hơn...");
                        } catch (Exception ex) {
                            System.err.println("Lỗi render trang " + (page + 1) + " ở DPI " + dpi + ": " + ex.getMessage());
                        }
                    }
                    if (img != null) {
                        final BufferedImage finalImg = img;
                        SwingUtilities.invokeLater(() -> {
                            JLabel lbl = new JLabel(new ImageIcon(finalImg));
                            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                            lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                            allPagesPanel.add(lbl);
                            allPagesPanel.revalidate();
                        });
                    } else {
                        final int pageIndex = page;
                        SwingUtilities.invokeLater(() -> {
                            JLabel errorLabel = new JLabel("Không thể render trang " + (pageIndex + 1));
                            errorLabel.setForeground(Color.RED);
                            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                            allPagesPanel.add(errorLabel);
                            allPagesPanel.revalidate();
                        });
                    }
                    if (isCancelled()) break;
                }
                return null;
            }

            @Override
            protected void done() {
                statusLabel.setText("Đã hiển thị toàn bộ " + totalPages + " trang");
                statusLabel.setForeground(Color.GRAY);
                allPagesPanel.revalidate();
                allPagesPanel.repaint();
            }
        };
        renderAllWorker.execute();
    }

    private void showError(String message) {
        if (continuousMode) {
            allPagesPanel.removeAll();
        } else {
            imagePanel.removeAll();
        }
        JLabel errorLabel = new JLabel("<html><center>" + message + "</center></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setVerticalAlignment(SwingConstants.CENTER);
        if (continuousMode) {
            allPagesPanel.add(errorLabel);
            allPagesPanel.revalidate();
            allPagesPanel.repaint();
        } else {
            imagePanel.add(errorLabel);
            imagePanel.revalidate();
            imagePanel.repaint();
        }
    }

    public void closeDocument() {
        cancelRender();
        cancelRenderAll();
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                System.err.println("Lỗi đóng PDF: " + e.getMessage());
            }
            document = null;
        }
        renderer = null;
        currentImage = null;
        totalPages = 0;
        currentPage = 0;
        statusLabel.setText(" ");
        continuousMode = false;
        controlPanel.setVisible(true);
        scrollPane.setViewportView(imagePanel);
    }

    private void cancelRender() {
        if (renderWorker != null && !renderWorker.isDone()) {
            renderWorker.cancel(true);
        }
    }

    private void cancelRenderAll() {
        if (renderAllWorker != null && !renderAllWorker.isDone()) {
            renderAllWorker.cancel(true);
        }
    }

    private int estimateDpi(BufferedImage image) {
        if (image == null) return -1;
        // PDFRenderer không trả về DPI thực tế, nên dùng kích thước tương đối để suy đoán.
        // Giá trị này chỉ để hiển thị trạng thái, không chính xác tuyệt đối.
        int width = image.getWidth();
        if (width >= 1600) return 200;
        if (width >= 1200) return 150;
        if (width >= 1000) return 120;
        if (width >= 900) return 100;
        if (width >= 800) return 96;
        return 72;
    }
    
    /**
     * Scale image để fit với chiều rộng viewport, giữ nguyên tỷ lệ
     * Giúp PDF tự động vừa với màn hình, không cần scroll ngang
     */
    private BufferedImage scaleImageToFitWidth(BufferedImage originalImage, int targetWidth) {
        if (originalImage == null || targetWidth <= 0) {
            return originalImage;
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Nếu image nhỏ hơn targetWidth, không cần scale
        if (originalWidth <= targetWidth) {
            return originalImage;
        }
        
        // Tính chiều cao mới giữ nguyên tỷ lệ
        int newWidth = targetWidth;
        int newHeight = (int) ((double) originalHeight * targetWidth / originalWidth);
        
        // Scale image với chất lượng tốt
        Image scaled = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        
        // Convert Image về BufferedImage
        BufferedImage scaledBuffered = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledBuffered.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        
        return scaledBuffered;
    }

}

