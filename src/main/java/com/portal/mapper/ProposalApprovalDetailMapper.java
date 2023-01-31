package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.dto.ProposalApprovalDetailDTO;
import com.portal.enums.ProposalRisk;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Model;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.Seller;
import com.portal.model.VehicleModel;

public class ProposalApprovalDetailMapper implements RowMapper<ProposalApprovalDetailDTO> {
    @Override
    public ProposalApprovalDetailDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        Person client = Person.builder()
                .id(rs.getInt("client_id"))
                .name(rs.getString("client_name"))
                .cnpj(rs.getString("client_cnpj"))
                .rne(rs.getString("clien_rne"))
                .rg(rs.getString("client_rg"))
                .cpf(rs.getString("client_cpf"))
                .build();

        Proposal proposal = Proposal.builder()
                .id(rs.getInt("pps_id"))
                .num(rs.getLong("pps_num"))
                .createDate(rs.getTimestamp("pps_create_date").toLocalDateTime())
                .build();

        Person partnerPerson = Person.builder()
                .id(rs.getInt("pptn_id"))
                .name(rs.getString("pptn_name"))
                .build();

        Partner partner = Partner.builder()
                .id(rs.getInt("ptn_id"))
                .person(partnerPerson)
                .build();

        Person sellerPerson = Person.builder()
                .id(rs.getInt("seller_person_id"))
                .name(rs.getString("seller_person_name"))
                .build();

        Seller seller = Seller.builder()
                .id(rs.getInt("sel_id"))
                .person(sellerPerson)
                .build();

        Brand brand = Brand.builder()
                .id(rs.getInt("brd_id"))
                .name(rs.getString("brd_name"))
                .build();

        Model model = Model.builder()
                .id(rs.getInt("mdl_id"))
                .name(rs.getString("mdl_name"))
                .build();

        ProposalDetailVehicle proposalDetailVehicle = ProposalDetailVehicle.builder()
                .id(rs.getInt("pdv_id"))
                .totalAmount(rs.getDouble("total_amount"))
                .build();

        VehicleModel vehicleModel = VehicleModel.builder()
                .id(rs.getInt("vhe_id"))
                .plate(rs.getString("plate"))
                .chassi(rs.getString("chassi"))
                .modelYear(rs.getInt("model_year"))
                .purchaseValue(rs.getDouble("purchase_value"))
                .model(model)
                .purchaseDate(rs.getTimestamp("purchase_date").toLocalDateTime().toLocalDate())
                .build();

        Channel channel = Channel.builder()
                .id(rs.getInt("channel_id"))
                .name(rs.getString("channel_name"))
                .build();

        return ProposalApprovalDetailDTO.builder()
                .client(client)
                .proposal(proposal)
                .partner(partner)
                .seller(seller)
                .brand(brand)
                .model(model)
                .proposalDetailVehicle(proposalDetailVehicle)
                .vehicle(vehicleModel)
                .risk(ProposalRisk.getById(rs.getInt("risk_id")))
                .channel(channel)
                .build();
    }
}
