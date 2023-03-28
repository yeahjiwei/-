package com.suo.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.suo.annotation.LoginRequired;
import com.suo.pojo.User;
import com.suo.service.FollowService;
import com.suo.service.LikeService;
import com.suo.service.UserService;
import com.suo.utils.CommunityConstant;
import com.suo.utils.CommunityUtil;
import com.suo.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private OSS ossClient;


    @Value("${alibaba.cloud.oss.bucket}")
    private String bucketName;

    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "你还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        if (filename == null) {
            model.addAttribute("error", "图片名错误");
            return "/site/setting";
        }
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
//        File dest = new File(uploadPath+"/"+filename);
        filename = "header/" + filename;
        try {
//            headerImage.transferTo(dest);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, headerImage.getInputStream());
            Map<String, String> map = new HashMap<>();
            //设置为公开读可见
            map.put("x-oss-object-acl", "public-read");
            putObjectRequest.setHeaders(map);
            ossClient.putObject(bucketName, filename, new ByteArrayInputStream(headerImage.getBytes()));
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        } finally {
            ossClient.shutdown();
        }
        //更新当前头像路径
        User user = hostHolder.getUser();
//        String headerUrl = getUrl(filename);
        String headerUrl = "https://" + bucketName + "." + endpoint + "/" + filename;
//        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

//    @LoginRequired
//    @RequestMapping("/setting")
//    public String getSettingPage(Model model) {
//
//        // 上传文件名称
//        String fileName = CommunityUtil.generateUUID();
//        // 设置响应信息
//        StringMap policy = new StringMap();
//        policy.put("returnBody", CommunityUtil.getJSONString(0));
//        // 生成上传凭证
//        Auth auth = Auth.create(accessKey, secretKey);
//        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
//
//        model.addAttribute("uploadToken", uploadToken);
//        model.addAttribute("fileName", fileName);
//        return "site/setting";
//
//
//    }


    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }


//    //废弃
//    @Deprecated
//    @LoginRequired
//    @RequestMapping(path = "/upload", method = RequestMethod.POST)
//    public String uploadHeader(MultipartFile headerImage, Model model) {
//        //判断上传文件是否为空
//        if (headerImage == null) {
//            model.addAttribute("error", "您还没有选择图片!");
//            return "site/setting";
//        }
//
//        //获取文件名的后缀
//        String fileName = headerImage.getOriginalFilename();
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        if (StringUtils.isBlank(suffix)) {
//            model.addAttribute("error", "文件的格式不正确!");
//            return "site/setting";
//        }
//
//        // 生成随机文件名
//        fileName = CommunityUtil.generateUUID() + suffix;
//        // 确定文件存放的路径
//        File dest = new File(uploadPath + "/" + fileName);
//
//        try {
//            //转存文件
//            headerImage.transferTo(dest);
//        } catch (IOException e) {
//            logger.error("上传文件失败: " + e.getMessage());
//            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
//        }
//
//        // 更新当前用户的头像的路径(web访问路径)
//        User user = hostHolder.getUser();
//        String headerUrl = domain + contextPath + "/user/header/" + fileName;
//        userService.updateHeader(user.getId(), headerUrl);
//
//        return "redirect:index";
//    }

    //废弃
    @Deprecated
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }


    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        if (StringUtils.isBlank(newPassword) || StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "密码密码不能为空");
            return "site/setting";
        }
        if (oldPassword.equals(newPassword)) {
            model.addAttribute("oldPasswordMsg", "新旧密码不能一致");
            return "site/setting";
        }
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())) {
            model.addAttribute("oldPasswordMsg", "密码错误");
            return "site/setting";
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());

        userService.updatePassword(user.getId(), newPassword);

        user.setPassword(newPassword);

        hostHolder.setUser(user);

        return "redirect:index";
    }

    // 个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);

        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "site/profile";
    }
}
