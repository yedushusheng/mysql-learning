public void testFetchSize()
{
	PreparedStatement ps = null;
	Connection conn = null;
	ResultSet rs = null;
	try {
		conn = DBUtils.getConnection();
		
		// 预处理添加数据
		ps = conn.prepareStatement("select * from db.t1");
		// 设置每次预取的条数
		ps.setFetchSize(1);
		rs = ps.executeQuery();
		while(rs.next()){
			// 输出结果
			System.out.println(rs.getString("job") + "\t" + rs.getString("emp_name"));
		}
	} catch(SQLException e) {
		e.printStackTrace();
	} finally {
		// 清理资源
		DBUtils.close(rs);
		DBUtils.close(ps);
		DBUtils.close(conn);
	}
}