public void updateEmp(){
	PreparedStatement ps = null;
	Connection conn = null;
	
	try{
		conn = DBUtils.getConnection();
		
		// 预处理添加数据
		ps = conn.prepareStatement("update db.t1 set sal=? where emp_name=?");
		
		// 设置参数
		ps.setFloat(1,(float)5000.0);
		ps.setString(2,"zhangsan");
		
		// 执行更新
		System.out.println("执行删除成功" + ps.executeUpdate() + "条");
	} catch (SQLException e){
		e.printStackTrace();
	} finially{
		// 清理资源
		DBUtils.close(ps);
		DBUtils.close(conn);
	}
}