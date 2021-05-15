public void insertEmp(){
	PreparedStatement ps = null;
	Connection conn = null;
	
	try{
		conn = DBUtils.getConnection();
		
		// 预处理添加数据
		ps = conn.prepareStatement("insert into tb.t1(emp_no,emp_name,job,hiredate,sal)" + "values(?,?,?,?,?)");
		
		// 设置参数
		ps.setInt(1,NumberUtils.getRandomInt());
		ps.setString(2,"zhangsan");
		ps.setString(3,"DBA");
		
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
		Date myDate2 = dateFormat2.parse("2010-09-13");
		ps.setFate(4,new java.sql.Date(myDate2.getTime()));
		ps.setFloat(5,(float)200.3);
		
		// 执行更新
		System.out.println("执行插入成功" + ps.executeUpdate() + "条");
	} catch (SQLException e){
		e.printStackTrace();
	} catch (ParseException e){
		e.printStackTrace();
	} finially{
		// 清理资源
		DBUtils.close(ps);
		DBUtils.close(conn);
	}
}