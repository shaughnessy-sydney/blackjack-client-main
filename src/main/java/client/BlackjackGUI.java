package client;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class BlackjackGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton;
    
    private JButton newSessionButton;
    private JButton reconnectButton;
    private JButton exitButton;

    private String BASE_URL = "http://euclid.knox.edu:8080/api/blackjack";
    private String USERNAME = "lashaughnessy";
    private String PASSWORD = "b64e760";
    private ClientConnecter clientConnecter;

    private CardPanel cardPanel;
    private Map<Card, ImageIcon> cardImages;
    private Random random = new Random();
    private UUID sessionId;

    public BlackjackGUI() {
        setTitle("Blackjack Game");
        setSize(1000, 800);
        loadCards();
        // create and pass the buttons to the card panel
        // it will resize them and add them to the panel

        //game buttons
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        hitButton.setVisible(false);
        standButton.setVisible(false);

        

        //additional menu buttons
        newSessionButton = new JButton("New Game");
        reconnectButton = new JButton("Resume");
        exitButton = new JButton("Exit");
        newSessionButton.setVisible(true);
        reconnectButton.setVisible(true);
        exitButton.setVisible(true);
        
        cardPanel = new CardPanel(hitButton, standButton, cardImages, 
            newSessionButton, reconnectButton, exitButton);
        setContentPane(cardPanel);
        
        
        
        // set the action listeners for the menu buttons
        newSessionButton.addActionListener(e -> {
            System.out.println("New Game clicked");
            try {
                GameState state = clientConnecter.startGame();
                sessionId = state.sessionId;
                state = promptAndPlaceBet();
                if (state != null) {
                    updateUIWithGameState(state);
                }
                hideMenuButtons();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error starting new game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        reconnectButton.addActionListener(e -> {
            System.out.println("Load clicked");
            //exit current session
            if (sessionId != null) {
                try{
                    clientConnecter.finishGame(sessionId);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error exiting game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // list sessions
            try {
                List<SessionSummary> sessionSummaryList = clientConnecter.listSessions();
                // Convert sessionSummaryList to a List<String> for display
                java.util.List<String> sessionStrings = new java.util.ArrayList<>();
                for (SessionSummary session : sessionSummaryList) {
                    sessionStrings.add("Session ID: " + session.sessionId + ", Balance: " + session.balance);               
                }
                showListPopup("Choose an item", sessionStrings);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        exitButton.addActionListener(e -> {
            if (sessionId != null) {
                try{
                    clientConnecter.finishGame(sessionId);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error exiting game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            System.exit(0);
        });


        

        // now set the action listeners for the hit/stand buttons
        hitButton.addActionListener(e -> {
            System.out.println("Hit button clicked");
            GameState state;
            try {
                state = clientConnecter.getGameState(sessionId);
                if(state.canHit) {
                    state = clientConnecter.hit(sessionId);
                    state = clientConnecter.getGameState(sessionId);
                    updateUIWithGameState(state);
            }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            repaint(); 
        });


        
        standButton.addActionListener(e -> {
            System.out.println("Stand button clicked");
            GameState state;
            try {
                state = clientConnecter.getGameState(sessionId);
                state = clientConnecter.stand(sessionId);
                state = clientConnecter.getGameState(sessionId);
                updateUIWithGameState(state);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            repaint(); 
        });

        

        // client connecter to make API calls on the server
        clientConnecter = new ClientConnecter(BASE_URL, USERNAME, PASSWORD);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addMenuBar();
        //TODO: keyboard shortcuts
        //TODO: mouse events
    }

    

    private void addMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Settings");
        
        menuBar.add(fileMenu);
        addMenuItem(fileMenu, "Reconnect", () -> {
            System.out.println("Load clicked");
            //exit current session
            if (sessionId != null) {
                try{
                    clientConnecter.finishGame(sessionId);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error exiting game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // list sessions
            try {
                List<SessionSummary> sessionSummaryList = clientConnecter.listSessions();
                // Convert sessionSummaryList to a List<String> for display
                java.util.List<String> sessionStrings = new java.util.ArrayList<>();
                for (SessionSummary session : sessionSummaryList) {
                    sessionStrings.add("Session ID: " + session.sessionId + ", Balance: " + session.balance);
                }
                showListPopup("Choose an item", sessionStrings);
                hideMenuButtons();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        addMenuItem(fileMenu, "New Game", () -> {
            System.out.println("New Game clicked");
                try {
                    GameState state = clientConnecter.startGame();
                sessionId = state.sessionId;
                state = promptAndPlaceBet();
                if (state != null) {
                    updateUIWithGameState(state);
                }
                hideMenuButtons();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error starting new game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        
        addMenuItem(fileMenu, "Exit", () -> {
            if (sessionId != null) {
                try{
                    clientConnecter.finishGame(sessionId);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error exiting game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            System.exit(0);
        });

    }

    // convert "THREE OF HEARTS" from server to Card.THREE_OF_HEARTS
    private Card getCard(String cardName) {
        //System.out.println("Card from server: '" + cardName + "'"); debug
        if (cardName == null) return null;
        Card card = Card.fromString(cardName);
        //System.out.println("Converted to enum: '" + card + "'"); debug
        return card;
    }

    private void addMenuItem(JMenu menu, String name, Runnable action) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(e -> action.run());
        menu.add(menuItem);
    }

    private void loadCards() {
        // Load card images and add them to the main panel
        // This is where you would implement the logic to load and display cards
        cardImages = new HashMap<>();
        for (Card card : Card.values()) {
            ImageIcon cardImage = new ImageIcon(getClass().getResource("/assets/" + card.getFilename()));
            cardImages.put(card, cardImage);
        }
    }

    private void hideMenuButtons() {
        newSessionButton.setVisible(false);
        reconnectButton.setVisible(false);
        exitButton.setVisible(false);
    }

    private void showMenuButtons() {
        newSessionButton.setVisible(true);
        reconnectButton.setVisible(true);
        exitButton.setVisible(true);
    }

    public void showListPopup(String title, java.util.List<String> items) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JList<String> list = new JList<>(new DefaultListModel<>());
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
        for (String item : items) {
            model.addElement(item);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(460); // Ensures each cell is wide

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // double click to select
                    String selected = list.getSelectedValue();
                    System.out.println("Selected: " + selected);

                    try {
                        // Extract session ID
                        String sessionIdString = selected.split(",")[0].replace("Session ID: ", "").trim();
                        UUID selectedSessionId = UUID.fromString(sessionIdString);

                        // Resume session
                        GameState state = clientConnecter.resumeSession(selectedSessionId);
                        sessionId = selectedSessionId;
                        hideMenuButtons();

                        // If no cards, prompt for bet and place it
                        boolean needsBet = (state.playerCards == null || state.playerCards.isEmpty())
                                        && (state.dealerCards == null || state.dealerCards.isEmpty());
                        if (needsBet) {
                            if (needsBet) {
                                state = promptAndPlaceBet();
                                if (state == null) return;
                            }
                        }
                        try {
                            Thread.sleep(200); // Wait 200 milliseconds
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        state = clientConnecter.getGameState(sessionId);

                        // Update UI
                        updateUIWithGameState(state);

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            BlackjackGUI.this, "Error resuming session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    dialog.dispose();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(480, 140)); // Make the popup wide

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.pack();
        dialog.setSize(500, 200); // Enforce minimum size if needed
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateUIWithGameState(GameState state) throws Exception {
        // Auto-reset if less than 5 cards left in the deck
        if (state.cardsRemaining < 5) {
            try {
                state = clientConnecter.resetGame(sessionId);
                JOptionPane.showMessageDialog(this, "Deck was low. Session has been reset.", "Deck Reset", JOptionPane.INFORMATION_MESSAGE);

                // Prompt for a new bet after reset
                state = promptAndPlaceBet();
                if (state == null) return; // User cancelled
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error resetting game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        cardPanel.clearCards();
        if (state.playerCards != null) {
            for (String cardName : state.playerCards) {
                cardPanel.addPlayerCard(getCard(cardName));
            }
        }
        if (state.dealerCards != null) {
            for (String cardName : state.dealerCards) {
                cardPanel.addDealerCard(getCard(cardName));
            }
        }

        repaint();

        //check if game is over
        if(state.gameOver) {
            //hide stand/hit buttons
            hitButton.setVisible(false);
            standButton.setVisible(false);
            // show results dialog
            showResultsDialog(state);
            try {
                clientConnecter.finishGame(sessionId);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error finishing game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            // show play again button (reconnect to session)
            // some of this code is redundant but it works
            int playAgain = JOptionPane.showConfirmDialog(this, "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
            if(playAgain == JOptionPane.YES_OPTION) {
                        state = clientConnecter.resumeSession(sessionId);
                        hideMenuButtons();
                    
                        // If no cards, prompt for bet and place it
                        boolean needsBet = (state.playerCards == null || state.playerCards.isEmpty())
                                        && (state.dealerCards == null || state.dealerCards.isEmpty());
                        if (needsBet) {
                            if (needsBet) {
                                state = promptAndPlaceBet();
                                if (state == null) return;
                            }
                        }
                        try {
                            Thread.sleep(200); // Wait 200 milliseconds
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        state = clientConnecter.getGameState(sessionId);

                        // Update UI
                        updateUIWithGameState(state);
            }
            else{
                clientConnecter.finishGame(sessionId);
                //show menu buttons
                showMenuButtons();
                //clear cards
                cardPanel.clearCards();

            }
        }
        repaint();

    }

    // adds a results dialog to the game
    private void showResultsDialog(GameState state) {
        String message = "Game Over!\n";
        //message += "Outcome: " + state.results() + "\n";
        message += "New Balance: " + state.balance + "\n";

        JOptionPane.showMessageDialog(this, message, state.results(), JOptionPane.INFORMATION_MESSAGE);
    }


    private GameState promptAndPlaceBet() throws Exception {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Enter your bet amount in multiples of 10:", "Place Bet", JOptionPane.PLAIN_MESSAGE);
            if (input == null) return null; // User cancelled
            try {
                int betAmount = Integer.parseInt(input.trim());
                if (betAmount <= 0) throw new NumberFormatException();
                GameState state = clientConnecter.placeBet(sessionId, betAmount);
                // Optionally, fetch the updated state after betting
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                //show hit/stand buttons
                hitButton.setVisible(true);
                standButton.setVisible(true);
                
                return clientConnecter.getGameState(sessionId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Bet", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        BlackjackGUI gui = new BlackjackGUI();
        gui.setVisible(true);
    }
    
}
