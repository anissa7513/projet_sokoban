import java.util.Scanner;

/*
 * PROJET SOKOBAN - niveau standard
 *
 * Le joueur ('@') pousse des caisses ('$') sur des objectifs ('.').
 * Il peut pousser une caisse, jamais la tirer.
 *
 * Le programme contient :
 *  - un menu avec plusieurs niveaux
 *  - le déplacement du joueur et des caisses, avec gestion des murs
 *  - la détection de la victoire
 *  - la validation des entrées
 *  - un compteur de mouvements
 */
public class Sokoban {

    // ==================== DONNÉES ====================

    // Sert à lire ce que l'utilisateur tape au clavier
    static Scanner sc = new Scanner(System.in);

    // Les niveaux. Chaque ligne a la même longueur et le niveau est entouré de murs.
    // '#' mur | ' ' case libre | '@' joueur | '$' caisse | '.' objectif | '*' caisse sur objectif
    static String[][] niveaux = {
        {   // Niveau 1
            "########",
            "#      #",
            "#  .   #",
            "#  $   #",
            "#  @   #",
            "#      #",
            "########"
        },
        {   // Niveau 2 : deux caisses
            "#######",
            "#     #",
            "# $ $ #",
            "# . . #",
            "#  @  #",
            "#######"
        },
        {   // Niveau 3 : plus difficile
            "####  ",
            "# .#  ",
            "#  ###",
            "#*@  #",
            "#  $ #",
            "#  ###",
            "####  "
        }
    };

    // On retient à part où sont les objectifs du niveau en cours.
    // objectifs[i][j] vaut true s'il y a un objectif à la ligne i, colonne j.
    // Comme ça, la grille de jeu ne contient que : '#', ' ', '@' et '$'.
    static boolean[][] objectifs;

    // Les 4 directions : 0 = haut, 1 = bas, 2 = gauche, 3 = droite
    static int[] dLigne   = {-1, 1, 0, 0};          // de combien la ligne change
    static int[] dColonne = {0, 0, -1, 1};          // de combien la colonne change
    static char[] touches = {'z', 's', 'q', 'd'};   // la touche de chaque direction

    // ==================== PROGRAMME PRINCIPAL ====================

    public static void main(String[] args) {
        boolean continuer = true;
        while (continuer) {
            afficherMenu();
            int choix = lireChoix(1, 4);
            if (choix == 4) {
                continuer = false;              // 4 = quitter : on sort de la boucle
            } else {
                jouer(choix - 1);               // niveau 1 = case 0 du tableau, etc.
            }
        }
        System.out.println("Au revoir !");
    }

    static void afficherMenu() {
        System.out.println();
        System.out.println("===== SOKOBAN =====");
        System.out.println("1. Niveau 1");
        System.out.println("2. Niveau 2");
        System.out.println("3. Niveau 3");
        System.out.println("4. Quitter");
        System.out.print("Votre choix : ");
    }

    // Demande un chiffre entre min et max et recommence tant que ce qui est tapé est invalide
    static int lireChoix(int min, int max) {
        while (true) {
            String saisie = sc.nextLine();
            if (saisie.length() == 1) {
                char c = saisie.charAt(0);
                if (c >= '0' + min && c <= '0' + max) {
                    return c - '0';             // transforme le caractère '3' en nombre 3
                }
            }
            System.out.print("Entrée invalide, tapez un chiffre entre " + min + " et " + max + " : ");
        }
    }

    // ==================== LE JEU ====================

    // Construit la grille de jeu du niveau numero (0 = niveau 1) et remplit "objectifs"
    static char[][] chargerNiveau(int numero) {
        String[] modele = niveaux[numero];
        int nbLignes = modele.length;
        int nbColonnes = modele[0].length();
        char[][] grille = new char[nbLignes][nbColonnes];
        objectifs = new boolean[nbLignes][nbColonnes];

        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                char c = modele[i].charAt(j);
                if (c == '.') {                  // objectif vide : case libre + objectif
                    grille[i][j] = ' ';
                    objectifs[i][j] = true;
                } else if (c == '*') {           // caisse sur objectif : caisse + objectif
                    grille[i][j] = '$';
                    objectifs[i][j] = true;
                } else {                         // mur, joueur, caisse ou case libre
                    grille[i][j] = c;
                }
            }
        }
        return grille;
    }

    // Affiche la grille. On remet les objectifs à l'affichage ('.', '*' ou '+').
    static void afficherGrille(char[][] grille) {
        for (int i = 0; i < grille.length; i++) {
            String ligne = "";
            for (int j = 0; j < grille[i].length; j++) {
                char c = grille[i][j];
                if (objectifs[i][j]) {
                    if (c == '$') {
                        c = '*';                 // caisse sur objectif
                    } else if (c == '@') {
                        c = '+';                 // joueur sur objectif
                    } else if (c == ' ') {
                        c = '.';                 // objectif vide
                    }
                }
                ligne = ligne + c;
            }
            System.out.println(ligne);
        }
    }

    // Cherche le joueur '@' et renvoie sa position : {ligne, colonne}
    static int[] trouverJoueur(char[][] grille) {
        int[] position = new int[2];
        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille[i].length; j++) {
                if (grille[i][j] == '@') {
                    position[0] = i;
                    position[1] = j;
                }
            }
        }
        return position;
    }

    // Le joueur a une caisse devant lui : la case derrière la caisse est-elle libre ?
    static boolean peutPousser(char[][] grille, int d) {
        int[] joueur = trouverJoueur(grille);
        int ligneDerriere = joueur[0] + 2 * dLigne[d];      // 2 cases devant le joueur
        int colonneDerriere = joueur[1] + 2 * dColonne[d];
        return grille[ligneDerriere][colonneDerriere] == ' ';
    }

    // Le joueur peut-il aller dans la direction d ?
    static boolean deplacementValide(char[][] grille, int d) {
        int[] joueur = trouverJoueur(grille);
        int ligneCible = joueur[0] + dLigne[d];             // case juste devant le joueur
        int colonneCible = joueur[1] + dColonne[d];
        char cible = grille[ligneCible][colonneCible];

        if (cible == '#') {
            return false;                                   // mur : impossible
        }
        if (cible == '$') {
            return peutPousser(grille, d);                  // caisse : possible si on peut la pousser
        }
        return true;                                        // case libre : possible
    }

    // Déplace la caisse située en (ligne, colonne) d'une case dans la direction d
    static void deplacerCaisse(char[][] grille, int ligne, int colonne, int d) {
        grille[ligne][colonne] = ' ';                                   // l'ancienne place devient libre
        grille[ligne + dLigne[d]][colonne + dColonne[d]] = '$';         // la caisse arrive à côté
    }

    // Déplace le joueur dans la direction d (on suppose que le déplacement est valide)
    static void deplacerJoueur(char[][] grille, int d) {
        int[] joueur = trouverJoueur(grille);
        int ligneCible = joueur[0] + dLigne[d];
        int colonneCible = joueur[1] + dColonne[d];

        if (grille[ligneCible][colonneCible] == '$') {
            deplacerCaisse(grille, ligneCible, colonneCible, d);        // on pousse la caisse d'abord
        }
        grille[joueur[0]][joueur[1]] = ' ';                             // le joueur quitte sa case
        grille[ligneCible][colonneCible] = '@';                         // il arrive sur la nouvelle
    }

    // Victoire : aucune caisse ne se trouve hors d'un objectif
    static boolean partieTerminee(char[][] grille) {
        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille[i].length; j++) {
                if (grille[i][j] == '$' && !objectifs[i][j]) {
                    return false;                   // une caisse n'est pas sur un objectif
                }
            }
        }
        return true;
    }

    // Renvoie l'indice de direction (0 à 3) d'une touche, ou -1 si la touche est invalide
    static int indexTouche(char touche) {
        for (int i = 0; i < touches.length; i++) {
            if (touches[i] == touche) {
                return i;
            }
        }
        return -1;
    }

    // Boucle de jeu pour un niveau
    static void jouer(int numero) {
        char[][] grille = chargerNiveau(numero);
        int mouvements = 0;

        while (true) {
            System.out.println();
            afficherGrille(grille);
            System.out.print("Commande (z/q/s/d, x pour quitter) : ");
            String saisie = sc.nextLine();

            if (saisie.length() != 1) {
                System.out.println("Commande invalide.");
                continue;                           // on recommence la boucle
            }
            char touche = saisie.charAt(0);

            if (touche == 'x') {
                System.out.println("Partie abandonnée.");
                return;                             // on quitte la méthode
            }

            int d = indexTouche(touche);
            if (d == -1) {
                System.out.println("Commande invalide.");
                continue;
            }
            if (!deplacementValide(grille, d)) {
                System.out.println("Déplacement impossible !");
                continue;
            }

            deplacerJoueur(grille, d);
            mouvements++;                           // on ne compte que les mouvements valides

            if (partieTerminee(grille)) {
                System.out.println();
                afficherGrille(grille);
                System.out.println("Victoire !");
                System.out.println("Nombre de mouvements : " + mouvements);
                return;
            }
        }
    }
}
