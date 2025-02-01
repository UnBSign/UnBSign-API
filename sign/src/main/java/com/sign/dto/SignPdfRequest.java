package com.sign.dto;

import org.springframework.web.multipart.MultipartFile;

public class SignPdfRequest {
    private MultipartFile file;
    private String id;
    private Float posX;
    private Float posY;
    private int pageNumber;
    
    public MultipartFile getFile() {
        return file;
    }
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public Float getPosX() {
        return posX;
    }
    public void setPosX(Float posX) {
        this.posX = posX;
    }
    public Float getPosY() {
        return posY;
    }
    public void setPosY(Float posY) {
        this.posY = posY;
    }
    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    
}
