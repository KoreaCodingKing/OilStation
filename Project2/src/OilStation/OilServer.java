package OilStation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class OilServer {

	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(10001);
			System.out.println("접속을 기다립니다.");
			while (true) {
				Socket sock = server.accept();
				// 클라이언트 각각 다른 스레드를 부여
				LoginThread loginThread = new LoginThread(sock);
				new Thread(loginThread).start();
			} // while
		} catch (Exception e) {
			System.out.println(e);
		}
	} // main
}

class LoginThread extends Thread {	
	// static 초기화 영역
	// static 필드를 초기화 하는 곳
	// 가장 먼저 호출됨
	static { // static영역에서 static 초기화
		// 1)DriverManager 내부에서 사용할 Oracle 드라이버 객체를 생성해서 메모리에 로딩함
		// 프로그램에서 직접 접근은 하지 않으므로 클래스 변수에 대입하지 않았다.
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	Scanner sc = new Scanner(System.in);
	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	PreparedStatement pstmt = null;

	private StringBuffer sb = new StringBuffer();
	private Socket sock = null;
	private String id = "";
	private BufferedReader br;
	private PrintWriter pw;
	private boolean check = true;
	private boolean flag = true;
	private boolean showme_flag = true;
	private boolean update_flag = true;
	private boolean confirm_flag = true;
	private boolean brand_flag = true;
	private boolean oil_flag = true;
	private boolean gu_flag = true;
	private boolean addr_flag = true;
	private boolean login_flag = true;

	public LoginThread(Socket sock) {
		this.sock = sock;
		try {
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println(pw + " 님이 접속");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			// 2)오라클에 접속을 한다
			/* Connection */

			// con =
			// DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe",
			// "bitjavadb", "bitjavadb");
			con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "bitcamp", "bitcamp");
			// localhost 다른사람 ip 넣으면 된다 , bitcamp는 각 id와pw에 해당

			// 3) 오라클과 대화를 하기 위한 Statement 생성
			/* Statement */
			stmt = con.createStatement();

			// 초기화면 UI 출력
			while (login_flag) {
				LoginUI();
			}
			// 로그인 이후 UI 메서드로 진입
			while (flag) {
				LoginedUI(id);

			}
			// 모든 소켓 close
			socketClose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void LoginUI() { // 초기 화면
		try {
			pw.println("======= [주유 예약 서비스에 오신걸 환영합니다] =======");
			pw.println("1. 로그인");
			pw.println("2. 회원가입");
			pw.println("3. 종료하기");
			pw.println("입력>> ");
			pw.flush();

			String sel = br.readLine();

			if (sel.equals("1")) { // 초기화면1.로그인 / Login() 호출
				Login();
				login_flag = false;
			} else if (sel.equals("2")) { // 초기화면2.회원가입/ MemberIn()호출
				MemberIn();
			} else if (sel.equals("3")) { // 초기화면3.종료하기/
				login_flag = false;
				flag = false;
				System.out.println(pw + " 님이  종료하셨습니다");
			} else {
				pw.println("잘못 입력하셨습니다.");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}// LoginUI

	// 1 로그인
	public void Login() {
		try {
			check = true;
			while (check) {
				String str = null;
				String str1 = null;
				// 로그인 아이디입력
				System.out.println(pw + "님이 로그인 시도중");
				pw.println("아이디를 입력하세요");
				pw.flush();
				String id = br.readLine();
				System.out.println(pw + "님이 로그인 아이디 " + id + " 입력");
				// 로그인 비밀번호입력
				pw.println("비밀번호를 입력하세요");
				pw.flush();
				String password = br.readLine();
				System.out.println(id + "님이  비밀번호 " + password + " 입력");

				sb.setLength(0);
				sb.append("SELECT id, pw FROM member ");
				sb.append("WHERE id='" + id + "'" + "AND pw = '" + password + "'");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					str = rs.getString(1);
					str1 = rs.getString(2);
				}
				if (id.equals(str) && password.equals(str1)) {
					this.id = id;
					pw.println("로그인 성공!");
					pw.println(id + "님 반갑습니다^^");
					System.out.println(id + "님이 로그인 하였습니다.");
					pw.flush();
					check = false;
					break;

				} else {
					pw.println("아이디/비밀번호를 다시 확인하세요");
					System.out.println(pw + "님이 아이디/비밀번호 틀림");
					pw.flush();
					continue;
				}

			} // while
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 2 회원가입
	public int MemberIn() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			String str = null;
			String str1 = null;
			check = true;
			while (check) {
				System.out.println(pw + "님이 회원가입 시도중");
				pw.println("사용할 아이디를 적으세요");
				pw.flush();
				String id = br.readLine();
				System.out.println(pw + "님이 새 아이디 " + id + " 입력");

				sb.setLength(0);
				sb.append("SELECT id FROM member ");
				sb.append("WHERE id = '" + id + "'");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();

				while (rs.next()) {
					str = rs.getString(1);
				}

				if (id.equals(str)) {
					pw.println("중복된 ID 입니다. 다른 ID를 입력하세요");
					pw.flush();
					System.out.println(pw + "님이 중복아이디를 입력");
					continue;
				} else {
					pw.println("사용할 비밀 번호를 입력하세요.");
					System.out.println(id + "님이 새 비밀번호 입력 시도중");
					pw.flush();
					String password = br.readLine();
					System.out.println(id + "님의  새 비밀번호: " + password);
					int len = password.length();
					char[] charr = new char[len];
					int count = 0;

					for (int i = 0; i < len; i++) {
						char charpass = password.charAt(i);
						charr[i] = charpass;
						if (charr[i] == '!' || charr[i] == '?' || charr[i] == '*' || charr[i] == '$' || charr[i] == '%'
								|| charr[i] == '^' || charr[i] == '&' || charr[i] == '@')
							count++;
					}
					// 비밀번호 8자리 이상, 특수문자 포함
					if (len < 8 && count < 2) {
						pw.println("비밀번호는 8자리 이상, 특수문자(!,@,#,$,%,^,&,*,) 포함하여 만드세요!");
						pw.flush();
						return MemberIn();
					}

					pw.println("핸드폰 번호를 입력하세요.");
					System.out.println(id + "님이 핸드폰 번호 입력 시도중");
					pw.flush();
					String phone = br.readLine();
					System.out.println(id + "님의 핸드폰 번호: " + phone);

					sb.setLength(0);
					sb.append("INSERT INTO MEMBER ");
					sb.append("VALUES ('" + id + "'");
					sb.append(",'" + password + "'");
					sb.append(",'" + phone + "')");
					pstmt = con.prepareStatement(sb.toString());

					rs = pstmt.executeQuery();

					pw.println("회원가입이 완료 되었습니다.");
					System.out.println(id + "님이 회원가입을 완료하였습니다");
					con.commit(); // 트랜잭션 완료 - 자바가 auto commit 한다
					pw.flush();

				}

				check = false;
				break;
			} // while
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	// 1-1 로그인 이후 메인화면
	public void LoginedUI(String id) {
		String Return_brand = "";
		String Return_oil = "";
		String Return_gu = "";
		try {
			pw.println("======= [원하시는 서비스를 선택하세요] =======");
			pw.println("1. 예약하기");
			pw.println("2. 예약 내역 확인");
			pw.println("3. 내 정보 확인");
			pw.println("4. 종료하기");
			pw.println("입력>> ");
			pw.flush();

			String sel = br.readLine();

			if (sel.equals("1")) {
				brand_flag = true;
				while (brand_flag) {
					Return_brand = selBrand();
					pw.println(Return_brand);
				}
				oil_flag = true;
				while (oil_flag) {
					Return_oil = selOil();
					pw.println(Return_oil);
				}

				gu_flag = true;
				while (gu_flag) {
					Return_gu = selGu(id);
					pw.println(Return_gu);
				}
				addr_flag = true;
				while (addr_flag) {
					String Return_addr = selAddr(Return_brand, Return_oil, Return_gu, id);
					pw.println(Return_addr);
					if (Return_addr != null) {
						int oil = oilPrice(Return_brand, Return_oil, Return_gu, Return_addr);
						oilPay(Return_gu, Return_addr, Return_brand, oil);
					} else {
						Return_brand = "";
						Return_oil = "";
						Return_gu = "";
						addr_flag = false;
						return;
					}
				}
			} else if (sel.equals("2")) {
				confirm_flag = true;
				while (confirm_flag) {
					confirm(id);
				}
			} else if (sel.equals("3")) {
				showme_flag = true; // showme() 재진입을 위함
				while (showme_flag) {
					showme(id);
				}
			} else if (sel.equals("4")) {
				flag = false;
				System.out.println(id + "님이 종료하셨습니다");
			} else {
				pw.println("잘못 입력하셨습니다.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}// LoginUI

	// 1-1-1 브랜드 선택 메서드(예약)
	public String selBrand() {
		int cnt_num = 0; // 출력시 번호 보이기 위함
		String sel_brand = ""; // 반환되는 상호 정보
		ArrayList<String> list = new ArrayList<String>(); // 문자열을 담을 리스트

		try {
			sb.setLength(0);
			sb.append("SELECT DISTINCT brand FROM oil_pro");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			pw.println("============ [예약하기] ============");
			pw.println("주유소 브랜드를 선택하세요");
			while (rs.next()) {
				list.add(new String(rs.getString(1)));
				pw.println((cnt_num + 1) + ". " + rs.getString(1));
				cnt_num++;
			}

			pw.println("선택 >> ");
			pw.flush();
			String sel = br.readLine();
			String strNum = sel;

			if (sel.equals(strNum)) {
				sel_brand = list.get(Integer.parseInt(strNum) - 1);
				brand_flag = false;
				return sel_brand;
			} else {
				pw.println("잘못입력했습니다.");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sel_brand;
	}// selBrand()

	// 1-1-2 유류 선택 메서드(예약)
	public String selOil() {
		String sel_oil = ""; // 반환되는 유류 정보
		try {
			pw.println("============ [예약하기] ============");
			pw.println("주유하실 유류를 선택하세요");
			pw.println("1. 고급 휘발유");
			pw.println("2. 휘발유");
			pw.println("3. 경유");
			pw.println("4. 실내 등유");
			pw.println("입력>> ");
			pw.flush();

			String sel = br.readLine();
			if (sel.equals("1")) {
				sel_oil = "pr_oil";
				oil_flag = false;
				return sel_oil;
			} else if (sel.equals("2")) {
				sel_oil = "oil";
				oil_flag = false;
				return sel_oil;
			} else if (sel.equals("3")) {
				sel_oil = "diesel";
				oil_flag = false;
				return sel_oil;
			} else if (sel.equals("4")) {
				sel_oil = "kerosene";
				oil_flag = false;
				return sel_oil;
			} else {
				pw.flush();
				// 잘못 입력시 다시 창 띄우기 생각
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sel_oil;
	}// selOil()

	// 1-1-3 구 선택 메서드(예약)
	public String selGu(String id) {
		String sel_gu = ""; // 반환되는 구 정보
		try {
			System.out.println(id + "님 주유 할 지역구 선택중");
			pw.println("============ [예약하기] ============");
			pw.println("주유하실 지역구를 선택하세요");
			pw.println("1. 강남구");
			pw.println("2. 강동구");
			pw.println("3. 강북구");
			pw.println("4. 강서구");
			pw.println("입력>> ");
			pw.flush();

			String sel = br.readLine();

			if (sel.equals("1")) {
				sel_gu = "강남구";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("2")) {
				sel_gu = "강동구";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("3")) {
				sel_gu = "강북구";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("4")) {
				sel_gu = "강서구";
				gu_flag = false;
				return sel_gu;
			} else {
				pw.println("잘못 입력하셨습니다.");
				pw.flush();
				// 잘못 입력시 다시 창 띄우기 생각
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sel_gu;
	}// selGu()

	// 1-1-4 도로명 선택 메서드 -> 쿼리문 : 정보출력(예약)
	public String selAddr(String sel_brand, String sel_oil, String sel_gu, String id) {
		int cnt_num = 0; // 출력시 번호 보이기 위함
		String sel_addr = ""; // 반환되는 주소 정보

		String brand = sel_brand; // 쿼리에 사용될 브랜드 정보를 담는 변수 brand
		String oil = sel_oil; // 쿼리에 사용될 기름 정보를 담는 변수 oil
		String gu = sel_gu; // 쿼리에 사용될 구 정보를 담는 변수 gu
		int q_cnt = 0;
		ArrayList<String> list = new ArrayList<String>(); // 문자열을 담을 리스트

		try {
			sb.setLength(0);
			sb.append("SELECT gu, name, addr, brand, phone, self, NVL(" + oil + ",0) FROM oil_pro ");
			sb.append("WHERE gu = " + "'" + gu + "'");
			sb.append("AND brand = " + "'" + brand + "'");
			sb.append("AND NVL(" + oil + ",0) != 0");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			System.out.println(id + "님 주유 할 주유소 선택중");
			pw.println("============[예약하기]============");
			pw.println("주유소를 선택하세요");

			while (rs.next()) {
				// list에 addr 정보 저장
				list.add(new String(rs.getString(3)));

				// gu, name, addr, brand, phone, self, oil정보 출력
				pw.println((cnt_num + 1) + ".\n지역구: " + rs.getString(1) + "\n이름: " + rs.getString(2) + "\n주소: "
						+ rs.getString(3) + "\n브랜드: " + rs.getString(4) + "\n전화번호: " + rs.getString(5) + "\n셀프유무: "
						+ rs.getString(6) + "\n리터당 가격: " + rs.getString(7) + " ");

				cnt_num++;
			}
			if (cnt_num > 0) {
				pw.println("선택 >> ");
				pw.flush();
				String sel = br.readLine();
				String strNum = sel;
				if (sel.equals(strNum)) {
					sel_addr = list.get(Integer.parseInt(strNum) - 1);
					addr_flag = false;
				}
			} else if (cnt_num == 0) {
				pw.println("데이터가 없습니다.");
				System.out.println("데이터가 없습니다.");

				addr_flag = false;
				sel_addr = null;
			} else {
				System.out.println("잘못 입력하셨습니다.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			pw.println("검색 결과가 없습니다");
			pw.flush();
			sel_addr = null;
		}
		return sel_addr;
	}// selAddr()

	// 1-1-5 유류 가격 저장 메서드(예약)
	public int oilPrice(String sel_brand, String sel_oil, String sel_gu, String sel_addr) {
		int price = 0; // 반환되는 상호 정보
		String gu = sel_gu; // 쿼리에 사용될 구 정보를 담는 변수 gu
		String addr = sel_addr; // 쿼리에 사용될 주소 정보를 담는 변수 addr
		String brand = sel_brand; // 쿼리에 사용될 구 정보를 담는 변수 gu
		String oil = sel_oil; // 쿼리에 사용될 주소 정보를 담는 변수 addr

		ArrayList<Integer> list = new ArrayList<Integer>(); // 문자열을 담을 리스트
		try {
			sb.setLength(0);
			sb.append("SELECT " + oil + " FROM oil_pro ");
			sb.append("WHERE gu = " + "'" + gu + "'");
			sb.append("AND brand = " + "'" + brand + "'");
			sb.append("AND addr = " + "'" + addr + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (rs.getString(1) == null) {
					list.add(new Integer(0));
				} else {
					list.add(new Integer(rs.getString(1)));
				}
			}

			price = list.get(0);

		} catch (Exception e) {
			System.out.println("잘못 입력하셨습니다.");
		}
		return price;
	}// oilPrice() 끝

	// 1-1-6 예약 최종 저장(예약)
	public void oilPay(String sel_gu, String sel_addr, String sel_brand, int sel_oil) {
		String gu = sel_gu; // 쿼리에 사용될 구 정보를 담는 변수 gu
		String addr = sel_addr; // 쿼리에 사용될 주소 정보를 담는 변수 addr
		String brand = sel_brand; // 쿼리에 사용될 상표 정보를 담는 변수 brand
		int oil_price = sel_oil;
		int input_oil = 0;

		try {
			System.out.println(id + "님 기름 가격 입력중");
			pw.println("============ [예약하기] ============");
			pw.println("기름을 얼마치 넣겠습니까? ");
			pw.println("입력 >> ");
			pw.flush();

			String sel = br.readLine();

			input_oil = Integer.parseInt(sel);

			pw.print(input_oil + "원 입력, ");
			pw.println((input_oil / sel_oil) + "리터를 주유 할 예정입니다.");
			pw.flush();

			Random rd = new Random();
			int resernum = rd.nextInt(1000) + 1000;

			String sid = ""; // 쿼리에 사용될 지점 번호

			sb.setLength(0);
			sb.append("SELECT id FROM oil_pro ");
			sb.append("WHERE addr = '" + sel_addr + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				sid = rs.getString(1);
			}
			sb.setLength(0);
			sb.append("INSERT INTO reserve ");
			sb.append("VALUES ('" + resernum + "'");
			sb.append(",'" + sid + "'");
			sb.append(",'" + id + "'");
			sb.append(",'" + sel_addr + "'");
			sb.append("," + input_oil / sel_oil + "");
			sb.append("," + input_oil + ")");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			pw.println("예약 번호는 " + resernum + " 입니다.");
			pw.println("예약이 완료 되었습니다.");
			System.out.println(id + " 님이 예약을 완료하였습니다");
			System.out.println("예약 번호는 " + resernum + " 입니다");
			pw.flush();
			addr_flag = false;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}// oilPay()

	// 1-1-2 예약 확인 메서드
	public void confirm(String id) { // 예약 확인 메서드
		int cnt_num = 0;
		try {
			sb.setLength(0);
			sb.append("SELECT * FROM reserve ");
			sb.append("WHERE memid = '" + id + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			pw.println("============ [예약 내역 확인] ============");

			while (rs.next()) {
				pw.println("----------------예약 " + (cnt_num + 1) + "-----------------");
				pw.println("예약번호 : " + rs.getString(1));
				pw.println("지점번호 : " + rs.getString(2));
				pw.println("아이디 : " + rs.getString(3));
				pw.println("주소 : " + rs.getString(4));
				pw.println("주유 예정량 : " + rs.getString(5) + "L");
				pw.println("-------------------------------------");
				cnt_num++;
			}

			if (cnt_num == 0) {
				pw.println("예약 정보가 없습니다.");
				System.out.println("예약 정보가 없습니다.");
				confirm_flag = false;
			} else {
				pw.println("1. 예약취소");
				pw.println("2. 뒤로가기");
				pw.println("입력>>");
				pw.flush();
				String ck = br.readLine();

				if (ck.equals("1")) {
					pw.println("예약 취소 할 예약번호를 입력하시오");
					pw.flush();
					String renum = br.readLine();

					sb.setLength(0);
					sb.append("SELECT renum FROM reserve ");
					sb.append("WHERE memid = '" + id + "'");
					pstmt = con.prepareStatement(sb.toString());
					rs = pstmt.executeQuery();

					while (rs.next()) {
						if (rs.getString(1).equals(renum)) {
							sb.setLength(0);
							sb.append("DELETE FROM reserve ");
							sb.append("WHERE renum = '" + renum + "' ");
							sb.append("AND memid = '" + id + "'");
							pstmt = con.prepareStatement(sb.toString());
							rs = pstmt.executeQuery();
							pw.println("취소되었습니다.");
							System.out.println(id + "님 예약 취소 완료");
							break;
						}
					}

					confirm_flag = false;
				} else if (ck.equals("2")) { // 2. 뒤로가기

					pw.println("메인화면으로 돌아갑니다");
					confirm_flag = false;
				} else {
					pw.println("잘못 입력하셨습니다.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // confirm 끝

	// 1-3 내 정보 확인 메서드
	public void showme(String id) {
		try {
			System.out.println("내 정보 확인중");
			pw.println("============ [내 정보 확인] ============");
			sb.setLength(0);
			sb.append("SELECT * FROM member ");
			sb.append("WHERE id='" + id + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pw.println("ID: " + rs.getString(1));
				pw.println("비밀번호: " + rs.getString(2));
				pw.println("핸드폰번호: " + rs.getString(3));
				pw.flush();
				break;
			}

			pw.println("1. 내 정보 수정");
			pw.println("2. 계정 삭제");
			pw.println("3. 뒤로가기");
			pw.println("입력>> ");
			pw.flush();

			String ck = br.readLine();
			if (ck.equals("1")) { // 1. 정보 수정
				update_flag = true; // update() 재 진입시를 위함
				while (update_flag) {
					update(id);
				}
				showme_flag = false; // showme 메서드 탈출
			} else if (ck.equals("2")) { // 2. 계정 삭제
				delete(id);
				showme_flag = false; // showme 메서드 탈출

			} else if (ck.equals("3")) { // 3. 뒤로가기
				showme_flag = false;
			} else {
				pw.println("잘못 입력");
			}

//         LoginUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // showme 끝

	// 1-3-1 내 정보 수정
	public void update(String id) {
		try {

			pw.println("============ [내 정보 수정] ============");
			pw.println("1. 비밀번호 수정");
			pw.println("2. 전화번호 수정");
			pw.println("3. 뒤로가기");
			pw.println("입력>> ");
			pw.flush();

			String ck = br.readLine();

			if (ck.equals("1")) { // 비밀번호 수정
				pw.println("수정할 비번 입력하시오.");
				System.out.println(id + "님이 비밀번호 수정 중");
				pw.flush();
				String pwd = br.readLine();
				System.out.println(id + "님의  새 비밀번호: " + pwd);
				int len = pwd.length();
				char[] charr = new char[len];
				int count = 0;

				for (int i = 0; i < len; i++) {
					char charpass = pwd.charAt(i);
					charr[i] = charpass;
					if (charr[i] == '!' || charr[i] == '?' || charr[i] == '*' || charr[i] == '$' || charr[i] == '%'
							|| charr[i] == '^' || charr[i] == '&' || charr[i] == '@')
						count++;
				}
				if (len < 8 && count < 2) {
					pw.println("비밀번호는 8자리 이상, 특수문자(!,@,#,$,%,^,&,*,) 포함하여 만드세요!");
					pw.flush();
				}

				sb.setLength(0);
				sb.append("UPDATE member ");
				sb.append("SET pw = '" + pwd + "' ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("수정되었습니다 초기화면으로 돌아갑니다");
				System.out.println(id + "님의 수정된 비밀번호: " + pwd);

				update_flag = false;

//            LoginedUI(id);
			} else if (ck.equals("2")) { // 핸드폰 번호 수정
				System.out.println(id + "님이 핸드폰 번호 수정 중");
				pw.println("수정할 핸드폰 번호를 입력하세요.");
				pw.flush();
				String phone = br.readLine();

				sb.setLength(0);
				sb.append("UPDATE member ");
				sb.append("SET phone = '" + phone + "' ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("수정되었습니다 초기화면으로 돌아갑니다");
				System.out.println(id + "님의 수정된 핸드폰 번호: " + phone);

				update_flag = false;
			} else if (ck.equals("3")) { // 3. 뒤로가기
				update_flag = false; // update_flag를 false로 만들어 while 탈출
			} else {
				pw.println("잘못 입력");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}// update 끝

	// 1-3-2 계정 삭제
	public void delete(String id) {
		try {
			System.out.println(id + "님이 계정 삭제 중");
			pw.println();
			pw.println("======= 정말로 삭제 하시겠습니까? =======");
			pw.println("1. 예");
			pw.println("2. 아니요");
			pw.println("입력>> ");
			pw.flush();

			String ck = br.readLine();

			if (ck.equals("1")) {

				sb.setLength(0);
				sb.append("DELETE FROM member ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("삭제되었습니다.");
				System.out.println(id + "님 계정 삭제 완료");
				System.out.println(id + "님이 종료하셨습니다");
				update_flag = false; // update 메서드 탈출
				showme_flag = false;

			} else if (ck.equals("2")) {
				pw.println("메인화면으로 돌아갑니다");
				update_flag = false; // update 메서드 탈출
			} else {
				pw.println("잘못 입력하셨습니다");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // delete 끝

	// 3 클라이언트 끊기
	public void socketClose() {
		try {
			flag = false;
			br.close();
			pw.close();
			sock.close();
		} catch (Exception ex) {
		}
	} // socketClose 끝
}// 스레드