<html>
    <head>
        <title>Welcome!</title>
    </head>
    <body>
        <form action="${contextPath}/upload" method="post" enctype="multipart/form-data">
            Submitter: <input type="text" name="submitter">
            File: <input type="file" name="file">
            <input type="submit" value="Submit">
        </form>
    </body>
</html>