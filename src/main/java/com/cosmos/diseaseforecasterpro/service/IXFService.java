package com.cosmos.diseaseforecasterpro.service;

import com.cosmos.diseaseforecasterpro.pojo.Result;
import org.springframework.web.multipart.MultipartFile;

public interface IXFService {


    Result ImageUnderstand(MultipartFile file,String uid, String text);

     String ImageGeneration(String uid,String content);
}
