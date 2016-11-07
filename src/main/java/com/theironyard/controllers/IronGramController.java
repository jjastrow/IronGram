package com.theironyard.controllers;

import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utilities.PasswordStorage;
import io.minio.MinioClient;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

@RestController
@PropertySource("classpath:application.properties")
public class IronGramController {
    @Value("${aws.accessid}")
    private String accessid;
    @Value("${aws.accesskey}")
    private String accesskey;
    @Value("${aws.bucket}")
    private String bucket;

    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    // start the TCP Server
    Server dbui;

    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();
    }

    @PreDestroy
    public void destroy() {
        dbui.stop();
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User login(String username, String password, HttpSession session, HttpServletResponse response) throws Exception {
        User user = users.findFirstByName(username);
        if (user == null) {
            user = new User(username, PasswordStorage.createHash(password));
            users.save(user);
        } else if (!PasswordStorage.verifyPassword(password, user.getPassword())) {
            throw new Exception("Wrong password");
        }
        session.setAttribute("username", username);
        response.sendRedirect("/");
        return user;
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public User getUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return users.findFirstByName(username);
    }

    @RequestMapping("/file-upload")
    public String handleFileUpload(HttpSession session, HttpServletResponse response, String receiver, MultipartFile photo) throws Exception {
        InputStream f = photo.getInputStream();

        System.out.println("\n\nBUCKET = " + bucket + "\nSECRET-KEY-ID = " + accessid + "\nFILENAME = " + photo.getOriginalFilename() + "\n\n");

        MinioClient s3Client = new MinioClient("https://s3.amazonaws.com", "AKIAIMRSY2HO4FZQ2RMA", "LWQQhL4XxGTv9OZd5AOph5mrnsKbmmeCKnobD95p");
        s3Client.putObject("com.theironyard.nashville", photo.getOriginalFilename(), f, f.available(), "application/octet-stream");
        f.close();

        return "redirect:/";
    }

    @RequestMapping("/upload")
    public Photo upload(
            HttpSession session,
            HttpServletResponse response,
            String receiver,
            MultipartFile photo
    ) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in.");
        }

        User senderUser = users.findFirstByName(username);
        User receiverUser = users.findFirstByName(receiver);

        if (receiverUser == null) {
            throw new Exception("Receiver name doesn't exist.");
        }

        if (!photo.getContentType().startsWith("image")) {
            throw new Exception("Only images are allowed.");
        }

        File photoFile = File.createTempFile("photo", photo.getOriginalFilename(), new File("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo();
        p.setSender(senderUser);
        p.setReceiver(receiverUser);
        p.setFilename(photoFile.getName());
        photos.save(p);

        response.sendRedirect("/");

        return p;
    }

    @RequestMapping("/photos")
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in.");
        }

        User user = users.findFirstByName(username);
        return photos.findByReceiver(user);
    }
}

