<%@ page import="java.io.*" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.json.simple.*" %>
<%@ page contentType="application/json" %>

<%
   //ensure the page returns a json object
   response.setContentType("application/json");

   //final strings for empty fields
   final String NO_TIME = "0";
   final String NO_NAME = "no name";
   final String NO_TITLE = "no title";
   final String NO_DESC = "no description";
   final String NO_QTY = "no qty";
   
   //response field declarations: the login id is mandatory for all activities 
   //filter and ACC_ID
   String handleFilter = request.getParameter("filter");
   String accId = request.getParameter("fb_id"); 	

   //RECIPE FIELDS
   String recipeCBType = request.getParameter("cookbook_type");
   String recipeName = request.getParameter("r_name");
   String recipeNameCurrent = request.getParameter("r_name_curr");
   String recipeId = request.getParameter("recipe_id");
   String imageURL = request.getParameter("image_url");

   //RECIPE INSTRUCTIONS FIELDS
   String recipeInstruction = request.getParameter("instruction");
   String recipeInstrStepNo = request.getParameter("step_no"); //int
   String recipeInstrTimeHr = request.getParameter("hrs"); //int
   String recipeInstrTimeMin = request.getParameter("mins"); //int
   String recipeDesc = request.getParameter("description");
   //INGREDIENTS FIELDS
   String ingRecName = request.getParameter("name");
   String ingName = request.getParameter("ingr_name");
   String ingQty = request.getParameter("quantity");

   //variables
   String userName = "";
   String query = "";
   String err = "";
   
   boolean isInDB = false;
   Connection conn = null;
   PreparedStatement pstmt = null, strstmt = null; 
   ResultSet rs = null, startSet = null;
   JSONObject result = new JSONObject();  
   JSONArray fields = new JSONArray();
   
   if(recipeInstrTimeHr == "") {
	  recipeInstrTimeHr = NO_TIME;
   }
   if( recipeInstrTimeMin == "") {
	  recipeInstrTimeMin = NO_TIME;   
   }
   if( recipeInstruction == "") {
	  recipeInstruction = NO_TITLE;   
   }
   if(recipeDesc == "") {
	  recipeDesc = NO_DESC;   
   }
   if( ingName == "") {
	 ingName = NO_NAME;   
   }
   if( ingQty == "") {
	  ingQty = NO_QTY;   
   }
   if( recipeName == "") {
	  recipeName = NO_NAME;   
   }
   try {
      //establish a connection with the SQL database
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/CookPal", "root", "");
      Statement stmt = conn.createStatement();

      //use name field to better display table association
      query = "SELECT * FROM account WHERE fb_id = ?";
      strstmt = conn.prepareStatement(query);      
      strstmt.setString(1, accId);
      startSet = strstmt.executeQuery();
      while(startSet.next()) {
         //if there is a match for fb id,    
         if(startSet.getString("fb_id").equals(accId))  {
            userName = startSet.getString("username"); 
         }
      }
      //actions
      //a new recipe wants to be added to the DB
      if(handleFilter.equals("insert_recipe")) {
         query = "INSERT INTO recipe (name, account_id, cookbook_type, image_url) VALUES (?, ?, ?, ?)";
         pstmt = conn.prepareStatement(query);     
		 
         conn.setAutoCommit(false);       
         pstmt.setString(1, recipeName);
         pstmt.setString(2, accId);
         pstmt.setString(3, recipeCBType);
         pstmt.setString(4, imageURL);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);         
      }  
      // retrieve all recipes for a certain user id
      else if(handleFilter.equals("select_recipes")) {
         query = "SELECT * FROM recipe WHERE account_id = ?";
         pstmt = conn.prepareStatement(query);          
         pstmt.setString(1, accId);    
         rs = pstmt.executeQuery();
         while(rs.next()) { 
            JSONObject kv = new JSONObject();
            kv.put("recipe name", rs.getString("name"));
            kv.put("cookbook status", rs.getString("cookbook_type"));
            kv.put("image", rs.getString("image_url"));
            fields.add(kv);
         }
         result.put(userName+"'s recipes:", fields);
         out.print(result);
         out.flush();
      }
      // deletes recipes with name for a certain user id
      // causes a cascading deletion that removes all corresponding ingredients and instructions
      else if(handleFilter.equals("delete_recipe")) { 
         int wat = 0;
         query = "DELETE FROM recipe WHERE account_id = ? AND name = ?";
         pstmt = conn.prepareStatement(query);   
         conn.setAutoCommit(false);
         pstmt.setString(1, accId);
         pstmt.setString(2, recipeName);
         err = "error in delete";
         wat = pstmt.executeUpdate(); 
         out.print(wat + " rows was successfully deleted");
         conn.commit();
         conn.setAutoCommit(true);     
		 
         query = "DELETE FROM recipe_ingredient WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);   
         
         conn.setAutoCommit(false);
         pstmt.setString(1, accId);
         pstmt.setString(2, recipeName);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);         
         
         query = "DELETE FROM recipe_instruction WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);   
         
         conn.setAutoCommit(false);
         pstmt.setString(1, accId);
         pstmt.setString(2, recipeName);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);                  
      }
      // inserts an ingredient to the corresponding recipe
      else if(handleFilter.equals("insert_ingredient")) {
         query = "INSERT INTO recipe_ingredient (recipe_name, account_id, name, quantity) VALUES (?, ?, ?, ?)";
         pstmt = conn.prepareStatement(query);     

         conn.setAutoCommit(false);       
         pstmt.setString(1, ingRecName);
         pstmt.setString(2, accId);
         pstmt.setString(3, ingName);
         pstmt.setString(4, ingQty);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);         
      }      
      // inserts an instruction to the corresponding recipe
      else if(handleFilter.equals("insert_instruction")) {
         query = "INSERT INTO recipe_instruction (recipe_name, account_id, instruction, hrs, mins, step_no, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
         pstmt = conn.prepareStatement(query);     
         conn.setAutoCommit(false);       
         pstmt.setString(1, ingRecName);
         pstmt.setString(2, accId);
         pstmt.setString(3, recipeInstruction);
         pstmt.setString(4, recipeInstrTimeHr);
         pstmt.setString(5, recipeInstrTimeMin);
         pstmt.setString(6, recipeInstrStepNo);         
         pstmt.setString(7, recipeDesc);
         pstmt.executeUpdate();
         conn.commit();
         conn.setAutoCommit(true);               
      }  
      //retrieving instructions to be used by the cook assistant/timer
      else if(handleFilter.equals("select_instruction")) {
         query = "SELECT * FROM recipe_instruction WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);          
         pstmt.setString(1, accId);  
         pstmt.setString(2, recipeName);
         rs = pstmt.executeQuery();
         while(rs.next()) {
            JSONObject kv = new JSONObject();
            kv.put("recipe name", rs.getString("recipe_name"));
            kv.put("instruction", rs.getString("instruction"));
            kv.put("hours", rs.getInt("hrs"));
            kv.put("minutes", rs.getInt("mins"));
            kv.put("step number", rs.getInt("step_no"));
            kv.put("description", rs.getString("description"));
            fields.add(kv);
         }
         result.put(userName+"'s recipe_instruction:", fields);
         out.print(result);
         out.flush();          
      }
      else if(handleFilter.equals("select_ingredient")) {
         query = "SELECT * FROM recipe_ingredient WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);          
         pstmt.setString(1, accId);  
         pstmt.setString(2, recipeName);
         rs = pstmt.executeQuery();
         while(rs.next()) {
            JSONObject kv = new JSONObject();
            kv.put("recipe name", rs.getString("recipe_name"));
            kv.put("ingredient", rs.getString("name"));
            kv.put("quantity", rs.getString("quantity"));
            fields.add(kv);
         }
         result.put(userName+"'s recipe_ingredients:", fields);
         out.print(result);
         out.flush();          
      }
	  //cascade delete recipe ingredients and instructions but update recipe table
	  else if(handleFilter.equals("update_recipe")) {	  
         query = "UPDATE recipe SET name = ?, cookbook_type = ?, image_url = ? WHERE account_id = ? AND name = ?";
         pstmt = conn.prepareStatement(query);     
		 
         conn.setAutoCommit(false);       
         pstmt.setString(1, recipeName);
         pstmt.setString(2, recipeCBType);
         pstmt.setString(3, imageURL);
		 pstmt.setString(4, accId);
		 pstmt.setString(5, recipeNameCurrent);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);     	
	  
		 query = "DELETE FROM recipe_ingredient WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);   
         
         conn.setAutoCommit(false);
         pstmt.setString(1, accId);
         pstmt.setString(2, recipeName);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);         
         
         query = "DELETE FROM recipe_instruction WHERE account_id = ? AND recipe_name = ?";
         pstmt = conn.prepareStatement(query);   
         
         conn.setAutoCommit(false);
         pstmt.setString(1, accId);
         pstmt.setString(2, recipeName);
         pstmt.executeUpdate(); 
         conn.commit();
         conn.setAutoCommit(true);   
	  }
   }   
   catch(SQLException e) {
	  out.print(err);
      e.printStackTrace();
   }
   finally {
  	// Release resources in a finally block in reverse-order of
  	// their creation
      if (startSet != null) {
         try {
            startSet.close();
         } catch (SQLException e) { } // Ignore
         startSet = null;
      }    
      if (rs != null) {
         try {
            rs.close();
         } catch (SQLException e) { } // Ignore
         rs = null;
      }    

      if (strstmt != null) {
         try {
            strstmt.close();
         } catch (SQLException e) { } // Ignore
         strstmt = null;
      }
      if (pstmt != null) {
         try {
            pstmt.close();
         } catch (SQLException e) { } // Ignore
         pstmt = null;
      }

      if (conn != null) {
         try {
            conn.close();
         } catch (SQLException e) { } // Ignore
         conn = null;
      }
   }

%>
