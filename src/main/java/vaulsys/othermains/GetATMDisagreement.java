package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetATMDisagreement {
	public static void main(String args[]) {
		List<BigDecimal> terminalCodes;
		terminalCodes = new ArrayList<BigDecimal>();

		
		terminalCodes.add(new BigDecimal(222137));
//		terminalCodes.add(new BigDecimal(216624));
//		terminalCodes.add(new BigDecimal(222083));
//		terminalCodes.add(new BigDecimal(222068));
//		terminalCodes.add(new BigDecimal(221584));
//		terminalCodes.add(new BigDecimal(221509));
//		terminalCodes.add(new BigDecimal(221426));
//		terminalCodes.add(new BigDecimal(220641));
//		terminalCodes.add(new BigDecimal(220625));
//		terminalCodes.add(new BigDecimal(220606));
//		terminalCodes.add(new BigDecimal(219883));
//		terminalCodes.add(new BigDecimal(219882));
//		terminalCodes.add(new BigDecimal(218309));
//		terminalCodes.add(new BigDecimal(218193));
//		terminalCodes.add(new BigDecimal(216650));
//		terminalCodes.add(new BigDecimal(216628));
//		terminalCodes.add(new BigDecimal(216608));
//		terminalCodes.add(new BigDecimal(216605));
//		terminalCodes.add(new BigDecimal(216293));
//		terminalCodes.add(new BigDecimal(215630));
//		terminalCodes.add(new BigDecimal(215508));
//		terminalCodes.add(new BigDecimal(215505));
//		terminalCodes.add(new BigDecimal(215368));
//		terminalCodes.add(new BigDecimal(215198));
//		terminalCodes.add(new BigDecimal(215196));
//		terminalCodes.add(new BigDecimal(215062));
//		terminalCodes.add(new BigDecimal(214408));
//		terminalCodes.add(new BigDecimal(213955));
//		terminalCodes.add(new BigDecimal(211323));
//		terminalCodes.add(new BigDecimal(207975));
//		terminalCodes.add(new BigDecimal());
//		terminalCodes.add(new BigDecimal());
//		terminalCodes.add(new BigDecimal());
//		terminalCodes.add(new BigDecimal());
//		terminalCodes.add(new BigDecimal(215196));
//		terminalCodes.add(new BigDecimal(215062));
//		terminalCodes.add(new BigDecimal(214408));
//		terminalCodes.add(new BigDecimal(213955));
//		terminalCodes.add(new BigDecimal(211323));
////		terminalCodes.add(new BigDecimal(223016));
////		terminalCodes.add(new BigDecimal(211321));
////		terminalCodes.add(new BigDecimal(221509));
////		terminalCodes.add(new BigDecimal(214408));
////		terminalCodes.add(new BigDecimal(221582));
////		terminalCodes.add(new BigDecimal(219883));


		
		System.out.print("terminal\taccount\tsandogh\tpardakhtshode\tbargashtshode\tdisagree\tmojoodi\tcoremojoodi\tcoredisagree\tpoolgozari");

		GeneralDao.Instance.beginTransaction();

		
//		terminalCodes = (List<BigDecimal>) GeneralDao.Instance.executeSqlQuery("select a.code from epay.term_atm a inner join epay.term_terminal t on t.code = a.code"  
//												+" where status = 1 and a.account is not null order by a.code");
		
//		for (Long terminal : terminalCodes) {
		for(int i=0; i<terminalCodes.size(); i++) {
			Long terminal = ((BigDecimal) terminalCodes.get(i)).longValue();
			if(terminal.equals(0L))
				break;

			if(terminal.equals(203003L) || terminal.equals(202247L))
				continue;

			String account = (String) GeneralDao.Instance.executeSqlQuery(
					"select account from epay.term_atm where code=" + terminal).get(0);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("terminal", terminal);
			params.put("account", account);
			params.put("date", DateTime.now().getDayDate().getDate());

			List<Object[]> queryResult = GeneralDao.Instance
					.executeSqlQuery(
							"select a.sandogh, nvl(b.pardakhtshode,0) as pardakhtshode,nvl(d.bargashtshode,0) as bargashtshode, "
									+ "coresanad.a-(c.sanad)-(a.sandogh)-nvl(b.pardakhtshode,0)+nvl(d.bargashtshode,0) as disagree,  "
									+ "coresanad.a-(c.sanad)+nvl(d.bargashtshode,0) as mojoodi,  "
									+ "coremojoodi.a as coremojoodi,   "
									+ "coresanad.a-c.sanad+nvl(d.bargashtshode,0)-coremojoodi.a as coredisagree, "
									+ "coresanad.a as poolgozari  "
									+ "from  "
									+ "(select sum(denomination * (notes+notes_rejected)) as sandogh from EPAY.TERM_ATM_DEVICE where ATM = :terminal "
									+ " and type like 'Cassette%') a,  "
									+ "(select sum(e.real_amt) as pardakhtshode "
									+ "from epay.trx_transaxion t   "
									+ "inner join epay.trx_flg_clearing c on t.src_clr_flg = c.id "
									+ "inner join epay.trx_flg_settlement s on s.id =  t.src_stl_flg "
									+ "inner join epay.ifx i on t.id = i.trx  "
									+ "inner join epay.ifx_emv_rq_data e on e.id = i.emvrqdata "
									+ "where i.terminal = :terminal  "
									+ "and i.direction = 1  "
									+ "and i.request = 0  "
									+ "and i.ifx_type = 107  "
									+ "and s.acc_state = 1  "
									+ "and i.recieved_date=:date "
									+ "and (c.clr_state = 1 or c.clr_state = 2))b, "
//									+ "and c.clr_state = 2)b, "
									+ "(select sum(s.amount) as bargashtshode  "
									+ "from EPAY.trx_transaxion t   "
									+ "inner join EPAY.trx_flg_clearing c on t.src_clr_flg = c.id "
									+ "inner join EPAY.trx_flg_settlement s on s.id =  t.src_stl_flg "
									+ "inner join EPAY.ifx i on t.id = i.trx  "
									+ "where i.terminal = :terminal  "
									+ "and i.direction = 1  "
									+ "and i.ifx_type = 107  "
									+ "and (s.acc_state = 3 or s.acc_state=6))d, "
//									 +
//									 "(select sum(totalamount) as sanad from epay.settlement_data s where terminal = :terminal and doc_num is null) d, "
									+ "(select sum(totalamount) as sanad from epay.settlement_data s where terminal = :terminal) c, "
									+ "(select sum(a*d) as a from (select sum(c_amount) as a, case c_debtor when 1 then 1 when 0 then -1 end as d from fcb.document_vw_haddad@fcb4 where c_accountnumber = :account group by c_debtor)) coremojoodi, "
									+ "(select sum(a*d) as a from (select sum(c_amount) as a, case c_debtor when 1 then 1 when 0 then -1 end as d from fcb.document_vw_haddad@fcb4 where c_accountnumber = :account and c_personnelcode <> 'switch'  "
									+ "and c_personnelcode <> '99990000'  " + "and c_personnelcode <> '99990004' "
									+ "and c_personnelcode <> '2887'  " + "and c_personnelcode <> '99990008' "
									+ "and c_personnelcode <> '3034'  " + "and c_personnelcode <> '1' " 
//									+ "and c_personnelcode <> '2164' "
									+ "group by c_debtor)) coresanad ", params);

			for (Object[] row : queryResult) {
				System.out.print("\r\n" + terminal + "\t"+account+"\t");
				for (Object col : row) {
					System.out.print(col + "\t");
				}
			}

		}
		GeneralDao.Instance.rollback();
		System.exit(0);
	}
}
