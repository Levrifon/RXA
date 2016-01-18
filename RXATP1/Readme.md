TP1 - Serveur de dialogue en ligne :

Exercice 2 :
Q1) On créer un nouveau Thread lorsqu'on réalise le accept du Master, le socket reçu est envoyé vers un Slave qui s'éxecute dans un thread séparé.

Q2) On utilise socket.getInputStream() pour stocker le flux d'entrée du socket dans un BufferedReader puis on utilise la méthode readLine() pour avoir l'avoir de façon textuelle.

Q3) On utilise la méthode repeterMessage dans la classe ServerMasterTCP qui possède une liste de tous ses slaves (et leur socket) et pour chacun socket on envoie le message.

Q4) Il suffit de rajouter une condition de type chaine.startWith("bye") pour détecter le bye ensuite il faut interrompre le thread et retirer le slave de la liste des sockets du master. (Ici j'utilise la méthode removeSlave(ServerSlaveTCP slave)
