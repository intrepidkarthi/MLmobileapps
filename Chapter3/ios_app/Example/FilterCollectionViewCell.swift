import UIKit

class FilterCollectionViewCell: UICollectionViewCell {
    @IBOutlet weak var lbl: UILabel!
    
    @IBOutlet weak var imageView: UIImageView!
    
    override func awakeFromNib() {
        self.layer.cornerRadius = 8
        self.layer.borderColor = UIColor.black.cgColor
        self.layer.borderWidth =  1
    }
    
}
