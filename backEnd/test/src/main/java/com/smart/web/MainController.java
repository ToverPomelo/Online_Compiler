/*
    Description: Controller.
*/

package com.smart.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.smart.service.MainService;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

@RestController
public class MainController {
	private MainService mainService;
	private final String PUBLIC_KEY_PATH = "/home/tover/Programs/SCNU/testSocket/test/src/main/webapp/resources/keys/public_key.der";
	private final String PRIVATE_KEY_PATH = "/home/tover/Programs/SCNU/testSocket/test/src/main/webapp/resources/keys/private_key.der";
	private OpenSSL openssl = new OpenSSL();
    private Gson gson = new Gson();
    private final String rootURL = "/home/tover/Programs/SCNU/testSocket/data/test";
	private final String chmod = "chmod 664 ";

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

    @RequestMapping("upload.html")
    public ModelAndView upload(HttpServletRequest req,HttpServletResponse rep){
        return new ModelAndView("upload");
    }

    @RequestMapping("file.html")
    public ModelAndView file(HttpServletRequest req,HttpServletResponse rep){
        return new ModelAndView("file");
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
/*
    @RequestMapping(value="uploadFiles",method = RequestMethod.POST)
    public String uploadFiles(HttpServletRequest req,HttpServletResponse rep){
	    String files = req.getParameter("files");

	    W2SAction fs = gson.fromJson(files, W2SAction.class);
	    //System.out.println(fs.toString());
		for(W2SData f : fs.getData()){
			final String fileName = rootURL + f.getPath();
			if(fileName.endsWith(File.separator)) return "not_a_file!";  //混进了目录名
		}

		for(W2SData f : fs.getData()){
			//System.out.println(f.getPath());
			//System.out.println(f.getContent());
			final String fileName = rootURL + f.getPath();
			//System.out.println(fileName);
			try{
				File file = new File(fileName);
				//System.out.println(file.getParentFile());

				if (!file.getParentFile().exists()){
					if(!file.getParentFile().mkdirs()) {
						return "mkdir_failed";  //创建上层目录失败
					}
				}

				if(!file.exists()){
					if(!file.createNewFile()){
						return "touch_failed";  //创建文件失败
					}
				}
				else{  //文件已存在
					//比较hash
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				out.write(f.getContent());
				out.flush();
				Runtime.getRuntime().exec(chmod+fileName,null).waitFor();
			}catch(Exception e){
				e.printStackTrace();
				return "failed";
			}
		}
	    return "uploaded";
    }

	private static List<String> getFilesOnServer(File file){
		File[] fs = file.listFiles();
		List<String> filesPath = new ArrayList<String>();
		for(File f:fs){
			if(f.isDirectory())	//若是目录，则递归打印该目录下的文件
				filesPath.addAll(getFilesOnServer(f));
			if(f.isFile())		//若是文件，直接打印
				filesPath.add(f.getPath());
		}
		return filesPath;
	}

	@RequestMapping(value="getFiles",method = RequestMethod.POST)
	public String getFiles(HttpServletRequest req,HttpServletResponse rep){
		W2SAction fs = new W2SAction();

		List<String> filesPath = getFilesOnServer(new File(rootURL));  //获取用户目录全部文件绝对路径
		//System.out.println(filesPath);
		//System.out.println(filesPath.get(1).substring(rootURL.length()));

		for(String fn : filesPath){
			try{
				W2SData f = new W2SData();
				f.setPath(fn.substring(rootURL.length()));

				StringBuffer buffer = new StringBuffer();
				InputStream is = new FileInputStream(fn);
				String line; // 用来保存每行读取的内容
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				line = reader.readLine(); // 读取第一行
				while (line != null) { // 如果 line 为空说明读完了
					buffer.append(line); // 将读到的内容添加到 buffer 中
					buffer.append("\n"); // 添加换行符
					line = reader.readLine(); // 读取下一行
				}
				reader.close();
				is.close();
				f.setContent(buffer.toString());

				fs.getData().add(f);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return gson.toJson(fs);
	}
*/


	@RequestMapping("ws.html")
	public ModelAndView ws(HttpServletRequest req,HttpServletResponse rep){
		return new ModelAndView("ws");
	}

	@Autowired
	public void setMainService(MainService mainService) {
		this.mainService = mainService;
	}
}
