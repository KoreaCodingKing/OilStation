package OilStation;

import java.net.*;         
import java.io.*;         
         
public class OilClient {         
         
   public static void main(String[] args) {      
      Socket sock = null;   
      BufferedReader br = null;
      PrintWriter pw = null;   
      try{   
         sock = new Socket("127.0.0.1", 10001);
         pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));      
         br = new BufferedReader(new InputStreamReader(sock.getInputStream()));      
         BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
         
         // ������� id�� �����Ѵ�.    
         InputThread it = new InputThread(sock, br, pw);      
         it.start();   
         String line = null;
         while((line = keyboard.readLine()) != null){   
            pw.println(line);   
            pw.flush();
         }
         System.out.println("Ŭ���̾�Ʈ�� ������ �����մϴ�.");      
      }catch(Exception ex){
         System.out.println(ex);
      }finally{
         try{
            if(pw != null)   
               pw.close();     
            if(br != null)   
               br.close();          
            if(sock != null)   
               sock.close();
         }catch(Exception ex){}      
      } // finally         
   } // main            
} // class               
               
class InputThread extends Thread{               
   private Socket sock = null;            
   private BufferedReader br = null;            
   private PrintWriter pw;
   
   public InputThread(Socket sock, BufferedReader br, PrintWriter pw){            
      this.sock = sock;         
      this.br = br;         
   }            
   public void run(){            
      try{         
         String line = null;      
         while((line = br.readLine()) != null){   
            System.out.println(line);   
         }      
      }catch(Exception ex){         
      }finally{         
         try{
            if(br != null)   
               br.close();
            if(pw != null)   
                pw.close(); 
            if(sock != null)   
               sock.close();
            System.out.println("�̿����ּż� �����մϴ�");
            System.exit(0);
         }catch(Exception ex){}      
      }         
   } // InputThread            
}   