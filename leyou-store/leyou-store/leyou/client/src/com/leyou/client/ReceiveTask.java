package com.leyou.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class ReceiveTask implements Runnable{
	private Socket socket;
	public ReceiveTask(Socket socket){
		this.socket=socket;
	}
	
	@Override
	public void run() {
		Random r=new Random();
		try {
			BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
			File parentFile = new File("//Users//wentimei//Desktop//学习资料");
			if(!parentFile.isDirectory()){
				parentFile.mkdirs();
			}
//			String ip=UUID.randomUUID().toString().replace("-", "");
			int random = r.nextInt(999);
			String ip=random+"";
			File file = new File(parentFile,ip+".rar");
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			byte [] buf=new byte[1024];
			int len=-1;
			while((len=bis.read(buf))!=-1){
				bos.write(buf,0,len);
				bos.flush();
			}
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write("文件传输成功".getBytes());
			socket.shutdownOutput();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
