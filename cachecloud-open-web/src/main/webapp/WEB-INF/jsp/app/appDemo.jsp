<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/manage/commons/taglibs.jsp" %>
<div class="row">
    <br/>
    <div id="dependency" class="highlight">
        <pre class="prettyprint">
            <c:forEach var="line" items="${dependency}">
            ${line}<br/>
            </c:forEach>
        </pre>
    </div>

    <div id="code" class="highlight">
        <pre class="prettyprint">
        <c:forEach var="line" items="${code}">
            ${line}<br/>
        </c:forEach>
        </pre>
    </div>
    
    <c:if test="${springConfig != null}">
	    <div id="springConfig" class="highlight">
	        <pre class="prettyprint">
	        <c:forEach var="line" items="${springConfig}">
	            ${line}<br/>
	        </c:forEach>
	        </pre>
	    </div>
    </c:if>
    
</div>
<script>
    prettyPrint();
</script>