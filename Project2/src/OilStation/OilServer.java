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
			System.out.println("������ ��ٸ��ϴ�.");
			while (true) {
				Socket sock = server.accept();
				// Ŭ���̾�Ʈ ���� �ٸ� �����带 �ο�
				LoginThread loginThread = new LoginThread(sock);
				new Thread(loginThread).start();
			} // while
		} catch (Exception e) {
			System.out.println(e);
		}
	} // main
}

class LoginThread extends Thread {	
	// static �ʱ�ȭ ����
	// static �ʵ带 �ʱ�ȭ �ϴ� ��
	// ���� ���� ȣ���
	static { // static�������� static �ʱ�ȭ
		// 1)DriverManager ���ο��� ����� Oracle ����̹� ��ü�� �����ؼ� �޸𸮿� �ε���
		// ���α׷����� ���� ������ ���� �����Ƿ� Ŭ���� ������ �������� �ʾҴ�.
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
			System.out.println(pw + " ���� ����");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			// 2)����Ŭ�� ������ �Ѵ�
			/* Connection */

			// con =
			// DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe",
			// "bitjavadb", "bitjavadb");
			con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "bitcamp", "bitcamp");
			// localhost �ٸ���� ip ������ �ȴ� , bitcamp�� �� id��pw�� �ش�

			// 3) ����Ŭ�� ��ȭ�� �ϱ� ���� Statement ����
			/* Statement */
			stmt = con.createStatement();

			// �ʱ�ȭ�� UI ���
			while (login_flag) {
				LoginUI();
			}
			// �α��� ���� UI �޼���� ����
			while (flag) {
				LoginedUI(id);

			}
			// ��� ���� close
			socketClose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void LoginUI() { // �ʱ� ȭ��
		try {
			pw.println("======= [���� ���� ���񽺿� ���Ű� ȯ���մϴ�] =======");
			pw.println("1. �α���");
			pw.println("2. ȸ������");
			pw.println("3. �����ϱ�");
			pw.println("�Է�>> ");
			pw.flush();

			String sel = br.readLine();

			if (sel.equals("1")) { // �ʱ�ȭ��1.�α��� / Login() ȣ��
				Login();
				login_flag = false;
			} else if (sel.equals("2")) { // �ʱ�ȭ��2.ȸ������/ MemberIn()ȣ��
				MemberIn();
			} else if (sel.equals("3")) { // �ʱ�ȭ��3.�����ϱ�/
				login_flag = false;
				flag = false;
				System.out.println(pw + " ����  �����ϼ̽��ϴ�");
			} else {
				pw.println("�߸� �Է��ϼ̽��ϴ�.");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}// LoginUI

	// 1 �α���
	public void Login() {
		try {
			check = true;
			while (check) {
				String str = null;
				String str1 = null;
				// �α��� ���̵��Է�
				System.out.println(pw + "���� �α��� �õ���");
				pw.println("���̵� �Է��ϼ���");
				pw.flush();
				String id = br.readLine();
				System.out.println(pw + "���� �α��� ���̵� " + id + " �Է�");
				// �α��� ��й�ȣ�Է�
				pw.println("��й�ȣ�� �Է��ϼ���");
				pw.flush();
				String password = br.readLine();
				System.out.println(id + "����  ��й�ȣ " + password + " �Է�");

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
					pw.println("�α��� ����!");
					pw.println(id + "�� �ݰ����ϴ�^^");
					System.out.println(id + "���� �α��� �Ͽ����ϴ�.");
					pw.flush();
					check = false;
					break;

				} else {
					pw.println("���̵�/��й�ȣ�� �ٽ� Ȯ���ϼ���");
					System.out.println(pw + "���� ���̵�/��й�ȣ Ʋ��");
					pw.flush();
					continue;
				}

			} // while
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 2 ȸ������
	public int MemberIn() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			String str = null;
			String str1 = null;
			check = true;
			while (check) {
				System.out.println(pw + "���� ȸ������ �õ���");
				pw.println("����� ���̵� ��������");
				pw.flush();
				String id = br.readLine();
				System.out.println(pw + "���� �� ���̵� " + id + " �Է�");

				sb.setLength(0);
				sb.append("SELECT id FROM member ");
				sb.append("WHERE id = '" + id + "'");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();

				while (rs.next()) {
					str = rs.getString(1);
				}

				if (id.equals(str)) {
					pw.println("�ߺ��� ID �Դϴ�. �ٸ� ID�� �Է��ϼ���");
					pw.flush();
					System.out.println(pw + "���� �ߺ����̵� �Է�");
					continue;
				} else {
					pw.println("����� ��� ��ȣ�� �Է��ϼ���.");
					System.out.println(id + "���� �� ��й�ȣ �Է� �õ���");
					pw.flush();
					String password = br.readLine();
					System.out.println(id + "����  �� ��й�ȣ: " + password);
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
					// ��й�ȣ 8�ڸ� �̻�, Ư������ ����
					if (len < 8 && count < 2) {
						pw.println("��й�ȣ�� 8�ڸ� �̻�, Ư������(!,@,#,$,%,^,&,*,) �����Ͽ� ���弼��!");
						pw.flush();
						return MemberIn();
					}

					pw.println("�ڵ��� ��ȣ�� �Է��ϼ���.");
					System.out.println(id + "���� �ڵ��� ��ȣ �Է� �õ���");
					pw.flush();
					String phone = br.readLine();
					System.out.println(id + "���� �ڵ��� ��ȣ: " + phone);

					sb.setLength(0);
					sb.append("INSERT INTO MEMBER ");
					sb.append("VALUES ('" + id + "'");
					sb.append(",'" + password + "'");
					sb.append(",'" + phone + "')");
					pstmt = con.prepareStatement(sb.toString());

					rs = pstmt.executeQuery();

					pw.println("ȸ�������� �Ϸ� �Ǿ����ϴ�.");
					System.out.println(id + "���� ȸ�������� �Ϸ��Ͽ����ϴ�");
					con.commit(); // Ʈ����� �Ϸ� - �ڹٰ� auto commit �Ѵ�
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

	// 1-1 �α��� ���� ����ȭ��
	public void LoginedUI(String id) {
		String Return_brand = "";
		String Return_oil = "";
		String Return_gu = "";
		try {
			pw.println("======= [���Ͻô� ���񽺸� �����ϼ���] =======");
			pw.println("1. �����ϱ�");
			pw.println("2. ���� ���� Ȯ��");
			pw.println("3. �� ���� Ȯ��");
			pw.println("4. �����ϱ�");
			pw.println("�Է�>> ");
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
				showme_flag = true; // showme() �������� ����
				while (showme_flag) {
					showme(id);
				}
			} else if (sel.equals("4")) {
				flag = false;
				System.out.println(id + "���� �����ϼ̽��ϴ�");
			} else {
				pw.println("�߸� �Է��ϼ̽��ϴ�.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}// LoginUI

	// 1-1-1 �귣�� ���� �޼���(����)
	public String selBrand() {
		int cnt_num = 0; // ��½� ��ȣ ���̱� ����
		String sel_brand = ""; // ��ȯ�Ǵ� ��ȣ ����
		ArrayList<String> list = new ArrayList<String>(); // ���ڿ��� ���� ����Ʈ

		try {
			sb.setLength(0);
			sb.append("SELECT DISTINCT brand FROM oil_pro");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			pw.println("============ [�����ϱ�] ============");
			pw.println("������ �귣�带 �����ϼ���");
			while (rs.next()) {
				list.add(new String(rs.getString(1)));
				pw.println((cnt_num + 1) + ". " + rs.getString(1));
				cnt_num++;
			}

			pw.println("���� >> ");
			pw.flush();
			String sel = br.readLine();
			String strNum = sel;

			if (sel.equals(strNum)) {
				sel_brand = list.get(Integer.parseInt(strNum) - 1);
				brand_flag = false;
				return sel_brand;
			} else {
				pw.println("�߸��Է��߽��ϴ�.");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sel_brand;
	}// selBrand()

	// 1-1-2 ���� ���� �޼���(����)
	public String selOil() {
		String sel_oil = ""; // ��ȯ�Ǵ� ���� ����
		try {
			pw.println("============ [�����ϱ�] ============");
			pw.println("�����Ͻ� ������ �����ϼ���");
			pw.println("1. ��� �ֹ���");
			pw.println("2. �ֹ���");
			pw.println("3. ����");
			pw.println("4. �ǳ� ����");
			pw.println("�Է�>> ");
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
				// �߸� �Է½� �ٽ� â ���� ����
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sel_oil;
	}// selOil()

	// 1-1-3 �� ���� �޼���(����)
	public String selGu(String id) {
		String sel_gu = ""; // ��ȯ�Ǵ� �� ����
		try {
			System.out.println(id + "�� ���� �� ������ ������");
			pw.println("============ [�����ϱ�] ============");
			pw.println("�����Ͻ� �������� �����ϼ���");
			pw.println("1. ������");
			pw.println("2. ������");
			pw.println("3. ���ϱ�");
			pw.println("4. ������");
			pw.println("�Է�>> ");
			pw.flush();

			String sel = br.readLine();

			if (sel.equals("1")) {
				sel_gu = "������";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("2")) {
				sel_gu = "������";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("3")) {
				sel_gu = "���ϱ�";
				gu_flag = false;
				return sel_gu;
			} else if (sel.equals("4")) {
				sel_gu = "������";
				gu_flag = false;
				return sel_gu;
			} else {
				pw.println("�߸� �Է��ϼ̽��ϴ�.");
				pw.flush();
				// �߸� �Է½� �ٽ� â ���� ����
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sel_gu;
	}// selGu()

	// 1-1-4 ���θ� ���� �޼��� -> ������ : �������(����)
	public String selAddr(String sel_brand, String sel_oil, String sel_gu, String id) {
		int cnt_num = 0; // ��½� ��ȣ ���̱� ����
		String sel_addr = ""; // ��ȯ�Ǵ� �ּ� ����

		String brand = sel_brand; // ������ ���� �귣�� ������ ��� ���� brand
		String oil = sel_oil; // ������ ���� �⸧ ������ ��� ���� oil
		String gu = sel_gu; // ������ ���� �� ������ ��� ���� gu
		int q_cnt = 0;
		ArrayList<String> list = new ArrayList<String>(); // ���ڿ��� ���� ����Ʈ

		try {
			sb.setLength(0);
			sb.append("SELECT gu, name, addr, brand, phone, self, NVL(" + oil + ",0) FROM oil_pro ");
			sb.append("WHERE gu = " + "'" + gu + "'");
			sb.append("AND brand = " + "'" + brand + "'");
			sb.append("AND NVL(" + oil + ",0) != 0");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			System.out.println(id + "�� ���� �� ������ ������");
			pw.println("============[�����ϱ�]============");
			pw.println("�����Ҹ� �����ϼ���");

			while (rs.next()) {
				// list�� addr ���� ����
				list.add(new String(rs.getString(3)));

				// gu, name, addr, brand, phone, self, oil���� ���
				pw.println((cnt_num + 1) + ".\n������: " + rs.getString(1) + "\n�̸�: " + rs.getString(2) + "\n�ּ�: "
						+ rs.getString(3) + "\n�귣��: " + rs.getString(4) + "\n��ȭ��ȣ: " + rs.getString(5) + "\n��������: "
						+ rs.getString(6) + "\n���ʹ� ����: " + rs.getString(7) + " ");

				cnt_num++;
			}
			if (cnt_num > 0) {
				pw.println("���� >> ");
				pw.flush();
				String sel = br.readLine();
				String strNum = sel;
				if (sel.equals(strNum)) {
					sel_addr = list.get(Integer.parseInt(strNum) - 1);
					addr_flag = false;
				}
			} else if (cnt_num == 0) {
				pw.println("�����Ͱ� �����ϴ�.");
				System.out.println("�����Ͱ� �����ϴ�.");

				addr_flag = false;
				sel_addr = null;
			} else {
				System.out.println("�߸� �Է��ϼ̽��ϴ�.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			pw.println("�˻� ����� �����ϴ�");
			pw.flush();
			sel_addr = null;
		}
		return sel_addr;
	}// selAddr()

	// 1-1-5 ���� ���� ���� �޼���(����)
	public int oilPrice(String sel_brand, String sel_oil, String sel_gu, String sel_addr) {
		int price = 0; // ��ȯ�Ǵ� ��ȣ ����
		String gu = sel_gu; // ������ ���� �� ������ ��� ���� gu
		String addr = sel_addr; // ������ ���� �ּ� ������ ��� ���� addr
		String brand = sel_brand; // ������ ���� �� ������ ��� ���� gu
		String oil = sel_oil; // ������ ���� �ּ� ������ ��� ���� addr

		ArrayList<Integer> list = new ArrayList<Integer>(); // ���ڿ��� ���� ����Ʈ
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
			System.out.println("�߸� �Է��ϼ̽��ϴ�.");
		}
		return price;
	}// oilPrice() ��

	// 1-1-6 ���� ���� ����(����)
	public void oilPay(String sel_gu, String sel_addr, String sel_brand, int sel_oil) {
		String gu = sel_gu; // ������ ���� �� ������ ��� ���� gu
		String addr = sel_addr; // ������ ���� �ּ� ������ ��� ���� addr
		String brand = sel_brand; // ������ ���� ��ǥ ������ ��� ���� brand
		int oil_price = sel_oil;
		int input_oil = 0;

		try {
			System.out.println(id + "�� �⸧ ���� �Է���");
			pw.println("============ [�����ϱ�] ============");
			pw.println("�⸧�� ��ġ �ְڽ��ϱ�? ");
			pw.println("�Է� >> ");
			pw.flush();

			String sel = br.readLine();

			input_oil = Integer.parseInt(sel);

			pw.print(input_oil + "�� �Է�, ");
			pw.println((input_oil / sel_oil) + "���͸� ���� �� �����Դϴ�.");
			pw.flush();

			Random rd = new Random();
			int resernum = rd.nextInt(1000) + 1000;

			String sid = ""; // ������ ���� ���� ��ȣ

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

			pw.println("���� ��ȣ�� " + resernum + " �Դϴ�.");
			pw.println("������ �Ϸ� �Ǿ����ϴ�.");
			System.out.println(id + " ���� ������ �Ϸ��Ͽ����ϴ�");
			System.out.println("���� ��ȣ�� " + resernum + " �Դϴ�");
			pw.flush();
			addr_flag = false;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}// oilPay()

	// 1-1-2 ���� Ȯ�� �޼���
	public void confirm(String id) { // ���� Ȯ�� �޼���
		int cnt_num = 0;
		try {
			sb.setLength(0);
			sb.append("SELECT * FROM reserve ");
			sb.append("WHERE memid = '" + id + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			pw.println("============ [���� ���� Ȯ��] ============");

			while (rs.next()) {
				pw.println("----------------���� " + (cnt_num + 1) + "-----------------");
				pw.println("�����ȣ : " + rs.getString(1));
				pw.println("������ȣ : " + rs.getString(2));
				pw.println("���̵� : " + rs.getString(3));
				pw.println("�ּ� : " + rs.getString(4));
				pw.println("���� ������ : " + rs.getString(5) + "L");
				pw.println("-------------------------------------");
				cnt_num++;
			}

			if (cnt_num == 0) {
				pw.println("���� ������ �����ϴ�.");
				System.out.println("���� ������ �����ϴ�.");
				confirm_flag = false;
			} else {
				pw.println("1. �������");
				pw.println("2. �ڷΰ���");
				pw.println("�Է�>>");
				pw.flush();
				String ck = br.readLine();

				if (ck.equals("1")) {
					pw.println("���� ��� �� �����ȣ�� �Է��Ͻÿ�");
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
							pw.println("��ҵǾ����ϴ�.");
							System.out.println(id + "�� ���� ��� �Ϸ�");
							break;
						}
					}

					confirm_flag = false;
				} else if (ck.equals("2")) { // 2. �ڷΰ���

					pw.println("����ȭ������ ���ư��ϴ�");
					confirm_flag = false;
				} else {
					pw.println("�߸� �Է��ϼ̽��ϴ�.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // confirm ��

	// 1-3 �� ���� Ȯ�� �޼���
	public void showme(String id) {
		try {
			System.out.println("�� ���� Ȯ����");
			pw.println("============ [�� ���� Ȯ��] ============");
			sb.setLength(0);
			sb.append("SELECT * FROM member ");
			sb.append("WHERE id='" + id + "'");
			pstmt = con.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pw.println("ID: " + rs.getString(1));
				pw.println("��й�ȣ: " + rs.getString(2));
				pw.println("�ڵ�����ȣ: " + rs.getString(3));
				pw.flush();
				break;
			}

			pw.println("1. �� ���� ����");
			pw.println("2. ���� ����");
			pw.println("3. �ڷΰ���");
			pw.println("�Է�>> ");
			pw.flush();

			String ck = br.readLine();
			if (ck.equals("1")) { // 1. ���� ����
				update_flag = true; // update() �� ���Խø� ����
				while (update_flag) {
					update(id);
				}
				showme_flag = false; // showme �޼��� Ż��
			} else if (ck.equals("2")) { // 2. ���� ����
				delete(id);
				showme_flag = false; // showme �޼��� Ż��

			} else if (ck.equals("3")) { // 3. �ڷΰ���
				showme_flag = false;
			} else {
				pw.println("�߸� �Է�");
			}

//         LoginUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // showme ��

	// 1-3-1 �� ���� ����
	public void update(String id) {
		try {

			pw.println("============ [�� ���� ����] ============");
			pw.println("1. ��й�ȣ ����");
			pw.println("2. ��ȭ��ȣ ����");
			pw.println("3. �ڷΰ���");
			pw.println("�Է�>> ");
			pw.flush();

			String ck = br.readLine();

			if (ck.equals("1")) { // ��й�ȣ ����
				pw.println("������ ��� �Է��Ͻÿ�.");
				System.out.println(id + "���� ��й�ȣ ���� ��");
				pw.flush();
				String pwd = br.readLine();
				System.out.println(id + "����  �� ��й�ȣ: " + pwd);
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
					pw.println("��й�ȣ�� 8�ڸ� �̻�, Ư������(!,@,#,$,%,^,&,*,) �����Ͽ� ���弼��!");
					pw.flush();
				}

				sb.setLength(0);
				sb.append("UPDATE member ");
				sb.append("SET pw = '" + pwd + "' ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("�����Ǿ����ϴ� �ʱ�ȭ������ ���ư��ϴ�");
				System.out.println(id + "���� ������ ��й�ȣ: " + pwd);

				update_flag = false;

//            LoginedUI(id);
			} else if (ck.equals("2")) { // �ڵ��� ��ȣ ����
				System.out.println(id + "���� �ڵ��� ��ȣ ���� ��");
				pw.println("������ �ڵ��� ��ȣ�� �Է��ϼ���.");
				pw.flush();
				String phone = br.readLine();

				sb.setLength(0);
				sb.append("UPDATE member ");
				sb.append("SET phone = '" + phone + "' ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("�����Ǿ����ϴ� �ʱ�ȭ������ ���ư��ϴ�");
				System.out.println(id + "���� ������ �ڵ��� ��ȣ: " + phone);

				update_flag = false;
			} else if (ck.equals("3")) { // 3. �ڷΰ���
				update_flag = false; // update_flag�� false�� ����� while Ż��
			} else {
				pw.println("�߸� �Է�");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}// update ��

	// 1-3-2 ���� ����
	public void delete(String id) {
		try {
			System.out.println(id + "���� ���� ���� ��");
			pw.println();
			pw.println("======= ������ ���� �Ͻðڽ��ϱ�? =======");
			pw.println("1. ��");
			pw.println("2. �ƴϿ�");
			pw.println("�Է�>> ");
			pw.flush();

			String ck = br.readLine();

			if (ck.equals("1")) {

				sb.setLength(0);
				sb.append("DELETE FROM member ");
				sb.append("WHERE id = '" + id + "' ");
				pstmt = con.prepareStatement(sb.toString());
				rs = pstmt.executeQuery();
				pw.println("�����Ǿ����ϴ�.");
				System.out.println(id + "�� ���� ���� �Ϸ�");
				System.out.println(id + "���� �����ϼ̽��ϴ�");
				update_flag = false; // update �޼��� Ż��
				showme_flag = false;

			} else if (ck.equals("2")) {
				pw.println("����ȭ������ ���ư��ϴ�");
				update_flag = false; // update �޼��� Ż��
			} else {
				pw.println("�߸� �Է��ϼ̽��ϴ�");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // delete ��

	// 3 Ŭ���̾�Ʈ ����
	public void socketClose() {
		try {
			flag = false;
			br.close();
			pw.close();
			sock.close();
		} catch (Exception ex) {
		}
	} // socketClose ��
}// ������