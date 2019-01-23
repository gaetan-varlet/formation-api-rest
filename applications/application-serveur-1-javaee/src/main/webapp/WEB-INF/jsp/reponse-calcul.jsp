<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Calcul avec JSP</title>
    </head>
    <body>
    	<p>Calcul avec affichage de la réponse dans la JSP :</p>
    	<p>La somme de ${param.nombre1} et ${param.nombre2} est égale à ${requestScope.somme}.</p>
    	<p>Le produit de ${param.nombre1} et ${param.nombre2} est égal à ${requestScope.produit}.</p>
    </body>
</html>