package com.smart.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
//import com.smart.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.smart.service.MainService;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MainController {
	private MainService mainService;
	private final String PUBLIC_KEY_PATH = "/home/tover/Programs/SCNU/testSocket/test/src/main/webapp/resources/keys/public_key.der";
	private final String PRIVATE_KEY_PATH = "/home/tover/Programs/SCNU/testSocket/test/src/main/webapp/resources/keys/private_key.der";
	private OpenSSL openssl = new OpenSSL();

	MainController(){
		try{
			openssl.loadPrivateKey(PRIVATE_KEY_PATH);
			openssl.loadPublicKey(PUBLIC_KEY_PATH);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	@RequestMapping("hello.html")
	public ModelAndView hello(HttpServletRequest req,HttpServletResponse rep){
		return new ModelAndView("hello");
	}

	@RequestMapping("key.html")
	public ModelAndView key(HttpServletRequest req,HttpServletResponse rep){
		return new ModelAndView("key");
	}

	@RequestMapping(value="getKey",method = RequestMethod.POST)
	//返回公钥给前段用于加密
	public String getKey(HttpServletRequest req,HttpServletResponse rep){
		try{
			if(!openssl.hasPubKey()) openssl.loadPublicKey(PUBLIC_KEY_PATH);
			return openssl.getPubKey();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}

	@RequestMapping(value="dealEnc",method = RequestMethod.POST)
	//解密前段加密信息，并转化为md5（通常用于密码）
	public void dealEnc(HttpServletRequest req,HttpServletResponse rep){
		final String m = req.getParameter("p");
		String message = m.replaceAll("\\s+","+");
		System.out.println("m: "+ message);
		try{
			if(!openssl.hasPriKey()) openssl.loadPrivateKey(PRIVATE_KEY_PATH);
			byte[] asBytes = Base64.getDecoder().decode(message);
			String decrypted = new String(openssl.decrypt(asBytes));

			byte[] b_decrypted = decrypted.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] b_md5 = md.digest(b_decrypted);
			String md5 = openssl.bytesToHex(b_md5);
			System.out.println("t: " + md5);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@RequestMapping("/uploadCode")
	@ResponseBody
	//上传代码
	public String uploadCode(HttpServletRequest request,String code){
		//tover:
		//写入代码文件
		//System.out.println(file);
		try{
			File writename = new File("/home/tover/Programs/SCNU/docker/users/29/code.cpp");
			writename.createNewFile(); // 创建新文件
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(code);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}

		return "uploaded";
	}

	@RequestMapping("ws.html")
	public ModelAndView ws(HttpServletRequest req,HttpServletResponse rep){
		return new ModelAndView("ws");
	}

	@Autowired
	public void setMainService(MainService mainService) {
		this.mainService = mainService;
	}
}
