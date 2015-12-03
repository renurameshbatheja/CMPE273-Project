<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>RedisApp Home Page</title>
        
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="js/redis.js" type="text/javascript"></script>

<link rel="stylesheet" href="/RedisWebapp/deco.css" type="text/css">

  
					
  <script>
function toggle() {
	 if( document.getElementById("hidethis").style.display=='none' ){
	   document.getElementById("hidethis").style.display = 'table-row'; // set to table-row instead of an empty string
	 }else{
	   document.getElementById("hidethis").style.display = 'table-row';
	 }
	}
</script> 
</head>
        

<body style="background-color:white">
<!-- <form id="redisAppForm" action="MainServlet">-->	


<table border="1" CELLPADDING="10" CELLSPACING="3" BORDERCOLOR="blue">


<tr bgcolor="#D3D3D3">
		<td width="1200px" colspan="2">
		
		
			<img src="images/Capture.PNG" height="122" width="252">
			<span style="font-family: tahoma; font-size: 20.5pt;"> <i><b>A Scalable Web Cache Using HRW Hashing On Top Of Redis</b></i></span>		
			</td>
	</tr>
	<tr bgcolor="#D3D3D3">
		<td width="600px">
			<form id="redisAppForm">						
					<table>
							<tr>
								<td colspan="2">
									<div align="center"><Strong>Add Or Remove Redis Server Instance</Strong></div>
								</td>
							</tr>
							<tr>
								<td colspan="2"><span class="errorMsg" style='color: red'></span>
								</td>
							</tr>
							<tr>
								<td align="right"><span
									style="font-family: tahoma; font-size: 10.5pt;"> <b>
											Host IP Address: </b></span></td>
								<td align="left"><input type="text" STYLE="font-family: Verdana; font-weight: bold; font-size: 12px;"
								 size="19" maxlength="60"
								id="hostIP" name="hostIP" /></td>
							</tr>
							<tr>
								<td align="left"><span
									style="font-family: tahoma; font-size: 10.5pt;"><b>
											Host Port: </b></span></td>
								<td align="left"><input type="text" STYLE="font-family: Verdana; font-weight: bold; font-size: 12px;
								 " size="19" maxlength="60" id="hostPort" name="hostPort" /></td>
							</tr>
							<tr>
								<td align="left"><span
									style="font-family: tahoma; font-size: 10.5pt;"><b>
											Host ID: </b></span></td>
								<td align="left"><input type="text" STYLE="font-family: Verdana; font-weight: bold; font-size: 12px;
								 " size="19" maxlength="60" id="ServerID" name="ServerID" /></td>
							</tr>							
							<tr>
								<td align="left"><span
									style="font-family: tahoma; font-size: 10.5pt;"><b>
											Add/Remove?: </b></span></td>
											
								<td align="left"> <select name="actionToBeTaken" id="actionToBeTaken">
								<option value="select">Select</option>
   								  <option value="add">Add</option>
  								  <option value="remove">Remove</option>
 								 </select></td>
							</tr>
							<tr>
								<td colspan="2">
									<input type="button"   class=button value="Add/Remove Node" id="AddOrRemove" name="addbtn" halign="right">
								</td>
							</tr>
							
							<tr>
								<td colspan="2">
									<br>
									<input type="button" class=button1 value="Configured Redis Server Instances" id="configuredInstances" name="configuredInstances" halign="right">
								</td>
							</tr>
											
						</table>
				
			</form>
		</td>
		<td width="600px">
			<form id="queryForm">	
					<table>
							<tr bgcolor="#D3D3D3">
								<td colspan="2">
									<div align="center"><Strong>Google Your Query String</Strong></div>
								</td>
							</tr>
					
							<tr>
								<td colspan="2"><span class="errorMsg" style='color: red'></span>
								</td>
							</tr>
							<tr>
								<td align="right"><span
									style="font-family: tahoma; font-size: 10.5pt;"> <b>
											Query String: </b></span></td>
								<td align="left"><input type="text" STYLE="font-family: Verdana; font-weight: bold; font-size: 12px;
" size="19" maxlength="60" id="queryString" name="queryString" /></td>
							</tr>
							<tr bgcolor="#D3D3D3">
								<td></td>
								<td>
									<input type="button" class="button2" value="Query Google" name="queryGoogle" halign="right" id="QueryGoogle" onclick="toggle()">
									
										
										<!-- "return toggletry('hidethis')"> -->
									<!--  onclick="toggleTable()"-->
									<!--<button onclick="myFunction()">Click me</button>  -->
								</td>
							</tr>
							

						</table>
			</form>
		</td>
	</tr>
	<tr bgcolor="#D3D3D3">
		<td id="serverInfoId">
									<div id="spinner" style="display:none;">
  										<img src="images/ajax-loader.gif" alt="Loading" />
									</div>					
									<div id="addedServerInformation"></div>
		</td>
		<td id="googleOutputId" style="height:0px;"> 
			<!--  <div id="hidethis"  style="display:none;">-->   				
							
									<div id="cacheHitOrMiss" style="display:none;position: absolute;top: 400px;"><strong>Output</strong></div>
									<div type="button" id="GoogleOutput" style="display:none;position: absolute;top: 430px;width:600px;height:auto;max-height:300px;overflow:auto;border:5px ridge green;float:centre"></div>
			<!--  </div>-->
		</td>
	</tr>
</table>




</body>

</html>
