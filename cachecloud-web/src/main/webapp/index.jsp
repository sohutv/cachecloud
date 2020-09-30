<% 
response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
String newLocation = "admin/app/list";
response.setHeader("Location",newLocation);
%>