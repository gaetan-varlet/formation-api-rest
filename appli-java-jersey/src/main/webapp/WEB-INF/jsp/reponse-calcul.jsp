<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Calcul avec JSP</title>
    </head>
    <body>
    	<p>Calcul avec affichage de la r�ponse dans la JSP :</p>
    	<p>La somme de ${param.nombre1} et ${param.nombre2} est �gale � ${requestScope.somme}.</p>
    	<p>Le produit de ${param.nombre1} et ${param.nombre2} est �gal � ${requestScope.produit}.</p>
    </body>
</html>
