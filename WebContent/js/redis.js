$(document).ready(function(){
	$('#configuredInstances').click(function(){
		
		   $.ajax({
							url: 'MainServlet', //your server side script
							data: {
								configuredInstances : 'true',
							}, //our data
							datatype: 'html',
							type: 'POST',
							beforeSend: function() {
								$('#addedServerInformation').html("");
								$('#spinner').show(); 
							},
					        complete: function() { 
					        	$('#spinner').hide(); 
					        },
							success : function(responseHTML) {
				                $('#addedServerInformation').html(responseHTML);
				            },
							error: function (jxhr, msg, err) {
								alert(err);
							}
			});
	});
	
	$('#AddOrRemove').click(function(){
					
					$.ajax({
									url: 'MainServlet', //your server side script
									data: {
										hostIP : $('#hostIP').val(),
										hostPort: $('#hostPort').val(),
										ServerID: $('#ServerID').val(),
										actionToBeTaken: $('#actionToBeTaken').val(),
									}, //our data
									datatype: 'html',
									type: 'POST',
									beforeSend: function() { 
										$('#addedServerInformation').html("");
										$('#spinner').show(); 
									},
							        complete: function() { 
							        	$('#spinner').hide(); 
							        },
									success : function(responseHTML) {
						                $('#addedServerInformation').html(responseHTML);
						            },
									error: function (jxhr, msg, err) {
										alert(err);
									}
					});
	});
		
	
	$('#QueryGoogle').click(function(){
		
		   $.ajax({
							url: 'MainServlet', //your server side script
							data: {
								queryString : $('#queryString').val(),
							}, //our data
							type: 'POST',
							datatype: 'json',
							beforeSend: function() {
								$('#cacheHitOrMiss').hide();
								$('#GoogleOutput').hide(); 
							},
					        complete: function() { 
					        	$('#googleOutputId').height('350px');
					        	$('#cacheHitOrMiss').show();
								$('#GoogleOutput').show();
					        },
							
							success : function(response) {
								if(response.HitOrMiss == 1) {
									$("#cacheHitOrMiss").html("<strong>Output : Cache Hit!</strong>");
								} else {
									$("#cacheHitOrMiss").html("<strong>Output : Cache Miss!</strong>");
								}
				                $("#GoogleOutput").html(response.Content);
				            },
							error: function (jxhr, msg, err) {
								alert(err);
							}
		   });
	});	
});

