<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Error page.</title>
        <meta charset="UTF-8">
    </head>
    <body>
         <jsp:text>
            error: ${message}
         </jsp:text>
    </body>
</html>