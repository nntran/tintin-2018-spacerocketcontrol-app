package fr.sqli.tintinspacerocketcontrolapp.player;

import java.util.List;

import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;
import it.auron.library.mecard.MeCard;

/**
 * Created by renaud on 16/06/17.
 */

public class PlayerFactory {


    public static  Player getPlayer(VCard vCard){
        Player player = new Player();
        if(vCard != null){
            List<Email> emails = vCard.getEmails();
            if (emails != null && emails.size() > 0){
                player.setEmail(emails.get(0).getValue());
            }
            StructuredName structuredName = vCard.getStructuredName();
            if(structuredName != null){
                player.setLastName(structuredName.getFamily());
                player.setFirstName(structuredName.getGiven());
            }
            Organization organizations = vCard.getOrganization();
            if(organizations != null && organizations.getValues() != null && organizations.getValues().size() > 0){
                player.setCompany(organizations.getValues().get(0));
            }
        }
        return player;
    }

    public static Player getPlayer(MeCard meCard){
        Player player = new Player();
        if(meCard != null){
            if (meCard.getEmail() != null){
                player.setEmail(meCard.getEmail());
            }
            if(meCard.getName() != null){
                player.setFirstName(meCard.getName());
            }
            if(meCard.getSurname() != null){
                player.setLastName(meCard.getSurname());
            }
            if(meCard.getOrg() != null){
                player.setCompany(meCard.getOrg());
            }
        }
        return player;
    }
}
