package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;

public class CardPanel extends JPanel 
{
    private static final long serialVersionUID = 1L;

    private JButton hitButton;
    private JButton standButton;

    private List<Card> dealerCards = new ArrayList<>();
    private List<Card> playerCards = new ArrayList<>();
    private Map<Card, ImageIcon> cardImages;
    //private Random random;

    private ImageIcon cardBackImage; // Add this field

    public CardPanel(JButton hitButton, JButton standButton, Map<Card, ImageIcon> cardImages)
    {
        this.hitButton = hitButton;
        this.standButton = standButton;
        this.cardImages = cardImages;

        // Load the card back image
        cardBackImage = new ImageIcon(getClass().getResource("/assets/back.png"));

        // null layout manager is absolute positioning
        setLayout(null);
        setBackground(Color.GREEN.darker());

        //loadCards();

        // add a hit and stand button
        // the actual click handler is defined in the BlackjackGUI class
        hitButton.setBounds(50, 600, 100, 60);
        add(hitButton);
        
        standButton.setBounds(200, 600, 100, 60);
        add(standButton);

    }

    public void clearCards() {
        dealerCards.clear();
        playerCards.clear();
    }

    public void addDealerCard(Card card) {
        dealerCards.add(card);
    }

    public void addPlayerCard(Card card) {
        playerCards.add(card);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 100;
        int y = 100;
        // dealer cards
        for (Card card : dealerCards) {
            if (card == null) {
                if (cardBackImage != null) {
                    g.drawImage(cardBackImage.getImage(), x, y, null);
                    x += cardBackImage.getIconWidth() + 10;
                }
            } else {
                // Draw dealer cards
                //System.out.println("Drawing dealer card: " + card);
                ImageIcon cardImage = cardImages.get(card);
                if (cardImage != null) {
                    System.out.println("Drawing dealer card: " + card);
                    g.drawImage(cardImage.getImage(), x, y, null);
                    x += cardImage.getIconWidth() + 10; 
                }
            }
            
            
        }

        // player cards
        x = 100; 
        y = 400;
        for (Card card : playerCards) {
            if (card == null) {
                if (cardBackImage != null) {
                    g.drawImage(cardBackImage.getImage(), x, y, null);
                    x += cardBackImage.getIconWidth() + 10;
                }
            } else {
                //System.out.println("Drawing player card: " + card);
                // Draw player cards
                ImageIcon cardImage = cardImages.get(card);
                if (cardImage != null) {
                    g.drawImage(cardImage.getImage(), x, y, null);
                    x += cardImage.getIconWidth() + 10; 
                }
            }
        }
    }        
    
}
